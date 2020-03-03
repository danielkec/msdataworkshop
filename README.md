"Intelligent Event-driven Stateful Microservices with Helidon and Autonomous Database on OCI" 

WORKSHOP - NOTE THAT THIS IS A WORK IN PROGRESS AND WILL BE COMPLETE BY MID MARCH 2020

![demo architecture](demo-arch.png) 

Task 1 (create OCI account, OKE cluster, ATP databases, and access OKE from cloud shell)
   - Get (free) OCI account and tenancy 
        - https://myservices.us.oraclecloud.com/mycloud/signup
        - note tenancy ocid, region name, user ocid
   - Create user api key and note the private key/pem location, fingerprint, and passphrase foobar 
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Functions/Tasks/functionssetupapikey.htm
   - Create compartment
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Identity/Tasks/managingcompartments.htm?Highlight=creating%20a%20comparment
        - https://oracle-base.com/articles/vm/oracle-cloud-infrastructure-oci-create-a-compartment#create-compartment
   - Create OCIR repos and auth key
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Registry/Tasks/registrycreatingarepository.htm
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Registry/Tasks/registrypushingimagesusingthedockercli.htm (login etc. steps can be done in later tasks)
   - Create OKE cluster
        - https://docs.cloud.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengcreatingclusterusingoke.htm
        - https://docs.cloud.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengaccessingclusterkubectl.htm
        - https://docs.cloud.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengdownloadkubeconfigfile.htm
   - Create 2 ATP-S pdbs named `orderdb` and `inventorydb` (for order and all other services)
        - If the pdbs are not named `orderdb` and `inventorydb` the deployment yamls in the examples will need to be modified to use the names given.
        - Select the license included option for license.
        - https://docs.oracle.com/en/cloud/paas/autonomous-data-warehouse-cloud/tutorial-getting-started-autonomous-db/index.html 
        - Note the ocid, compartmentId, name, and admin pw of the databases
        - Download the regional wallet (connection info) and note the wallet password (this is optional depending on setup - todo elaborate)
   - Enter cloud shell and issue command to export kubeconfig for the OKE cluster created
        - Related blog with quick instructions here: https://blogs.oracle.com/cloud-infrastructure/announcing-oracle-cloud-shell
        - Verify OKE access using command such as `kubectl get pods --all-namespaces`
        - Create `msdataworkshop` namespace using command `kubectl create ns msdataworkshop`
    
    
Task 2 (create github account and build microservice image)
   - Create github account
        - http://github.com
   - From cloud shell...
   - Run `git clone https://github.com/paulparkinson/msdataworkshop.git`
        - optionally (if planning to make modifications, for example) fork this repos and Run `git clone` on the forked repos
   - `cd msdataworkshop`
   - Run `./build.sh`
   - For convenience, `source shortcutaliases` or add the contents to the `~/.bash_profile` file and source it.
        - Run 'usage' for list of shortcut commands include `msdataworkshop` command which lists all resources related to the msdataworkshop


Task 3 (push image, deploy, and access microservice)
   - Setup OCIR, create authkey
   - From cloud shell...
   - `docker login` 
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Registry/Tasks/registrypushingimagesusingthedockercli.htm
        - example `docker login us-phoenix-1.ocir.io` user: msdataworkshoptenancy/msdataworkshopuser password: [authtoken]
   - Modify the following files... (todo get this from single location such as DEMOREGISTRY env var)
        - export DEMOREGISTRY setting it to OCIR repos location such as us-phoenix-1.ocir.io/stevengreenberginc/paul.parkinson/msdataworkshop
        - edit pom.xml and replace <docker.image.prefix>us-phoenix-1.ocir.io/stevengreenberginc/paul.parkinson/msdataworkshop</docker.image.prefix>
        - edit `./deploy.sh` and replace us-phoenix-1.ocir.io/stevengreenberginc/paul.parkinson/msdataworkshop/frontend-helidon:0.1
   - Run `./build.sh` to push images to OCIR
   - Mark the images as public in OCIR via Cloud Console - todo if this is an issue mod deployment/git to do `docker login`
   - Run `kubectl create ns msdataworkshop` 
   - Run `./deploy.sh` to create deployment and service to namespace msdataworkshop created in previous step
   - Check frontend pod is Running by using `kubectl get pods --all-namespaces`
   - Check frontend loadbalancer address using `kubectl get services --all-namespaces`
   - Access frontend page 
        - via frontend LoadBalancer service, eg http://129.146.94.70:8080
        - or, if using NodePort instead of LoadBalancer as service do..
            - `kubectl port-forward [frontend pod] -n msdataworkshop 8080:8080`
            - and access http://localhost:8080


Task 4 (Setup OCI Open Service Broker)
   - Refer to https://github.com/oracle/oci-service-broker and specifically...
        - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/installation.md
        - https://www.youtube.com/watch?v=qW_pw6Nd5hM&t=12s
   - Run ./installOSB.sh
   - If not already created, create user API Key with password...
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Functions/Tasks/functionssetupapikey.htm
   - Note that the following instructions will create the broker in the default namespace 
        and then create the binding and users secrets in the `msdataworkshop` namespace
   - Modify `./ocicredentialsSecret` supplying values from Task 1 
   - Run `./ocicredentialsSecret`
   - Run `./installOCIOSB.sh`
   - Run `svcat get brokers` 
        - recheck until broker is shown in ready state
   - Run `svcat get classes` and `svcat get plans` 
   
Task 5 (Using OCI service broker, create binding to 2 existing atp instances, and verify with test/admin app for both)
   - Insure Task 4 is complete and refer to https://github.com/oracle/oci-service-broker and specifically...
        - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/atp.md
        - https://www.youtube.com/watch?v=qW_pw6Nd5hM&t=12s
   - Do the following for each/both ATP instances/pdbs (*replace `order` with `inventory` for second/inventory ATP instance/pdb)
        - cd to `oci-service-broker` directory such as oci-service-broker-1.3.3
        - `cp samples/atp/atp-existing-instance.yaml atp-existing-instance-order.yaml`
        - modify `atp-existing-instance-order.yaml` 
            - provide class and plan name and pdb ocid and compartmentID
        - Run `kubectl create -f atp-existing-instance-order.yaml``
        - Run `svcat get instances` 
            - verify in ready state
        - `cp oci-service-broker/samples/atp/atp-binding-plain.yaml atp-binding-plain-order.yaml` 
        - modify `atp-binding-plain-order.yaml` 
            - provide wallet password (either new or existing, for example if downloaded from console previously)
        - Run `kubectl create -f atp-binding-plain-order.yaml -n msdataworkshop`
        - Run `svcat get bindings` 
            - verify in ready state
        - Run `kubectl get secrets atp-demo-binding -n msdataworkshop -o yaml` 
        - `cp oci-service-broker/samples/atp/atp-demo-secret.yaml atp-demo-secret-order.yaml` 
        - modify `atp-demo-secret-order.yaml` 
            - provide admin password and wallet password (use `echo -n value | base64` to encode)
        - Run `kubectl create -f atp-demo-secret-order.yaml -n msdataworkshop`
   - Insure Task 2 (create github account and build microservice image) is complete.
   - `cd msdataworkshop/osb-atp-dbadmin-helidon`
   - Notice `atp1` references in microprofile-config.properties and @Inject dataSource in OrderResource.java 
   - Notice deployment yamls' wallet, secret, decode initcontainer, etc. 
   - Run `./deploy.sh` to create deployment and service
   - Run `msdataworkshop` command to verify existence of deployment and service and verify pod is in Running state
   - Demonstrate service discovery/call to order and inventory db 
        and db access from these services using `executeonorderpdb` and `executeoninventorypdb` on frontend
   - Troubleshooting... 
        - Look at logs... `kubectl logs [podname] -n msdataworkshop`
        - If no request is show in logs, try accessing the pod directly using port-forward
            - `kubectl port-forward [order pod] -n msdataworkshop 8080:8080`
            - http://localhost:8080/placeOrder
            

Task 6 (setup AQ, order and inventory, saga, and CQRS)...
   - follow steps to create dblink across ATP instances (put/get credential from object store etc.)
   - setup AQ, queue-progation - todo should be all one button to create orderuser, inventoryuser, tables, queue, dblink, propagation, etc...
   - create secrets for orderuser and inventoryuser and update deployment yaml 
   - mvn install SODA and AQ jars
   - create order, inventory, and supplier deployments and services
   - todo from frontpage app select create ordertoinventory propagation
   - todo from frontpage app select create inventorytoorder propagation
   - demonstrate placeorder for choreography saga (success and fail/compensate)
   - demonstrate showorder for CQRS
   
Task 7 (Using OCI service broker, provision and create binding to stream, and verify with app)
   - Insure Task 4 is complete and refer to https://github.com/oracle/oci-service-broker and specifically...
        - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/oss.md
   - In Cloud Console and streaming policy
        - add a group for user if one does not exist
        - add policy for that group to allow streaming (eg name `StreamingPolicy`, description `Allow to manage streams`)
            - Policy statement `Allow group <SERVICE_BROKER_GROUP> to manage streams in compartment <COMPARTMENT_NAME>`
            - eg `Allow group msdataworkshop-admins to manage streams in compartment msdataworkshop-sandbox)`' 
   - todo... Currently hitting some issues with the following and resorting to manually setting 
   - cd to `oci-service-broker` directory such as oci-service-broker-1.3.3
   - `cp samples/oss/create-oss-instance.yaml create-oss-instance-order.yaml`
   - Modify `create-oss-instance-order.yaml` 
        - change name to `teststreamorder` provide compartmentID and specify `1` partition
   - Run `kubectl create -f create-oss-instance-order.yaml -n msdataworkshop`
   - `cp samples/oss/create-oss-binding.yaml create-oss-binding-order.yaml`
   - Modify `create-oss-binding-order.yaml` 
        - change name to `test-stream-binding-order` and change instanceRef name to `teststream-order'
   - Run `kubectl create -f create-oss-binding-order.yaml -n msdataworkshop`
   - Run `kubectl get secrets test-stream-binding-order -o yaml -n msdataworkshop`
   - Demonstrate streaming orders in frontend app by hitting `producerstream` button

Task 8 (demonstrate health/readiness) 
   - eg order service is not ready until some data load (from view or eventsourcing or lazily) is done
   - show src and probes in deployment
   - https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/
   - http://heidloff.net/article/implementing-health-checks-microprofile-istio
   - https://github.com/oracle/helidon/blob/master/docs/src/main/docs/health/02_health_in_k8s.adoc
   - https://github.com/oracle/helidon/blob/master/docs/src/main/docs/guides/07_health_se_guide.adoc
   - https://dmitrykornilov.net/2019/08/08/helidon-brings-microprofile-2-2-support/
    
Task 9 (demonstrate metrics prometheus and grafana (maybe monitoring and alert)
   - show compute auto-scaling in console before explaining horizontal scaling of pods.
        - for reference re compute instance scaling... https://docs.cloud.oracle.com/en-us/iaas/Content/Compute/Tasks/autoscalinginstancepools.htm
   - https://medium.com/oracledevs/how-to-keep-your-microservices-available-by-monitoring-its-metrics-d88900298025
   - https://learnk8s.io/autoscaling-apps-kubernetes
   - high level: https://itnext.io/kubernetes-monitoring-with-prometheus-in-15-minutes-8e54d1de2e13
   - https://github.com/coreos/prometheus-operator/blob/master/Documentation/user-guides/getting-started.md
   - helm repo update ;  helm install stable/prometheus-operator --name prometheus-operator --namespace monitoring
   - https://medium.com/oracledevs/deploying-and-monitoring-a-redis-cluster-to-oracle-container-engine-oke-5f210b91b800
   - helm install --namespace monitoring stable/prometheus-operator --name prom-operator --set kubeDns.enabled=true --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false --set coreDns.enabled=false --set kubeControllerManager.enabled=false --set kubeEtcd.enabled=false --set kubeScheduler.enabled=false
   - kubectl get pods  -n monitoring
   - k create -f OrderServiceServiceMonitor.yaml -n msdataworkshop
   - kubectl port-forward -n monitoring prometheus-prometheus-operator-prometheus-0 9090
   - kubectl -n monitoring get pods | grep grafana
   - kubectl -n monitoring port-forward [podname] 3000:3000
   - Login with admin/prom-operator
   - https://kubernetes.io/docs/tasks/Run-application/horizontal-pod-autoscale-walkthrough/#autoscaling-on-multiple-metrics-and-custom-metrics
   - https://github.com/helm/charts/tree/master/stable/prometheus-adapter
   - helm install --name my-release stable/prometheus-adapter

Task 10 (demonstrate OKE horizontal pod scaling)
   - install metrics-server
        - DOWNLOAD_URL=$(curl -Ls "https://api.github.com/repos/kubernetes-sigs/metrics-server/releases/latest" | jq -r .tarball_url)
        - DOWNLOAD_VERSION=$(grep -o '[^/v]*$' <<< $DOWNLOAD_URL)
        - curl -Ls $DOWNLOAD_URL -o metrics-server-$DOWNLOAD_VERSION.tar.gz
        - mkdir metrics-server-$DOWNLOAD_VERSION
        - tar -xzf metrics-server-$DOWNLOAD_VERSION.tar.gz --directory metrics-server-$DOWNLOAD_VERSION --strip-components 1
        - kubectl apply -f metrics-server-$DOWNLOAD_VERSION/deploy/1.8+/
   - kubectl get pods -n msdataworkshop |grep order-helidon
   - kubectl top pod order-helidon-74f848d85c-gxfq7 -n msdataworkshop --containers 
   - kubectl autoscale deployment order-helidon --cpu-percent=50 --min=1 --max=2 -n msdataworkshop
   - kubectl get hpa -n msdataworkshop
            NAME            REFERENCE                  TARGETS         MINPODS   MAXPODS   REPLICAS   AGE
            order-helidon   Deployment/order-helidon   <unknown>/50%   1         2         0          16s
   - increase cpu, notice cpu increase and scale to 2 pods

Task 11 (tracing)
   - install istio, demonstrate tracing (jaeger and kiali)
   - @Traced annotation

Task 12 (demonstrate scaling with pdb)
    - autoscaling
    - sharding
  
  
  
Future here to end...
 
Task 13 
   - various security both of wire, mtls, vault, etc. 
   - analytics - OSE server visualization
   - message to logic/endpoint mapping
   - kafka streams not just in chunks theres no end
   - rehydation / retention and compacted queues (most recent not all events) time windows
   - comes from functional aspect, no sharding orders based on phone number, more memory ?)_
   - https://medium.com/oracledevs/how-to-keep-your-microservices-available-by-monitoring-its-metrics-d88900298025
   - nice to have: 
        - messaging when available, JPA, JTA
        - fn
        - cloud developer service
        - apex report/callout to helidon
        - ORDS
        - Grafana of OCI https://blogs.oracle.com/cloudnative/data-source-grafana
        - graph route planning
        kms key monitoring

Task 14 (data flow) 
    - fully managed Spark service that lets you Run Spark applications with almost no administrative overhead.
    - for demo: fog computing of IoT
    
Task 15 (data science)
    - enables data scientists to easily build, train, and manage machine learning models on Oracle Cloud, using Python and open source machine learning libraries
    - for demo: predictive analytics of orders to inventory/delivery locations

Task 16 (data catalog)
    - enables data consumers to easily find, understand, govern, and track Oracle Cloud data assets across the enterprise using an organized inventory of data assets
    - what data is available where in the organization and how trustworthy and fit-for-use they are
    - for demo: analytics report of order info from streaming + atp 

