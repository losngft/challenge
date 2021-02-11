Some basic steps to do before going to production
1) The port to call the urls is 18080
    Example:// localhost:18080/v1/accounts/
    {"accountId":"Id-123","balance":1000}
    {"accountId":"Id-124","balance":500}
    Example:// localhost:18080/v1/transfers/
    {"accountFromId":"Id-123","accountToId":"Id-124","amount":1000}
2) Generate the api.yaml
3) Implement the NotificationService interface otherwise it will not gonna work
4) Keep just the necessary files in the project
5) Continuous integration, BDD tests
6) Communicating with the business(all areas affected)
7) Deployment plan (cut over plan)
8) Rollback plan
9) Deployment exercise
10) Generate the artifact(release)