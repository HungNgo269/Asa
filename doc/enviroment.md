PS C:\Users\mhung\IdeaProjects\untitled> docker run --rm hello-world  
Unable to find image 'hello-world:latest' locally  
latest: Pulling from library/hello-world  
4f55086f7dd0: Pull complete   
d5e71e642bf5: Download complete   
Digest: sha256:5e23090353324d887c48ad5e5c56d294eab81588df9605b07d1afe895f9cc8f8  
Status: Downloaded newer image for hello-world:latest

Hello from Docker!  
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
1. The Docker client contacted the Docker daemon.
2. The Docker daemon pulled the "hello-world" image from the Docker Hub.  
   (amd64)
3. The Docker daemon created a new container from that image which runs the  
   executable that produces the output you are currently reading.
4. The Docker daemon streamed that output to the Docker client, which sent it  
   to your terminal.

To try something more ambitious, you can run an Ubuntu container with:  
$ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:  
https://hub.docker.com/

For more examples and ideas, visit:  
https://docs.docker.com/get-started/

PS C:\Users\mhung\IdeaProjects\untitled> docker run --name weldjobs-postgres-test -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16-alpine  
Unable to find image 'postgres:16-alpine' locally  
16-alpine: Pulling from library/postgres  
509688edf37b: Pull complete   
d8d21be4bf02: Pull complete   
a88782f92806: Pull complete   
6117bd39f23f: Pull complete   
81717818649a: Pull complete   
e2de96513ba9: Pull complete   
1b7f9cd581ee: Pull complete   
210b280f9556: Pull complete   
6c25da34e03f: Pull complete   
3ae6141cfd99: Pull complete   
25b77f59fd95: Pull complete   
e6b147246a89: Download complete   
021b01ea4cc2: Download complete   
Digest: sha256:721873c34ceb9f8d8fc265984940dc982404c105f19ad51be9fdc5970a6080ea  
Status: Downloaded newer image for postgres:16-alpine  
e26cd2076b54261a2a701dc9881f898e8f24b34d8c1ada975ecdeb7105e65f58  
PS C:\Users\mhung\IdeaProjects\untitled> docker exec -it weldjobs-postgres-test psql -U postgres -c "SELECT version();"  
version
------------------------------------------------------------------------------------------  
PostgreSQL 16.15 on x86_64-pc-linux-musl, compiled by gcc (Alpine 15.2.0) 15.2.0, 64-bit  
(1 row)

PS C:\Users\mhung\IdeaProjects\untitled> docker stop weldjobs-postgres-test  
weldjobs-postgres-test  
PS C:\Users\mhung\IdeaProjects\untitled> docker rm weldjobs-postgres-test  
weldjobs-postgres-test  
PS C:\Users\mhung\IdeaProjects\untitled>   