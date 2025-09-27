## TASK

Ethereum transactions crawler task
Create an application that will allow a user to view transaction data from the Ethereum blockchain associated with a specific wallet address W that the user inputs, starting with block B. The application should get information on:
wallets (addresses) and
amounts of ETH associated with transactions made to and from the given wallet W and
show them in a simple human-readable way (ideally, through a web page).
The application should collect and display ALL transaction data starting from the given block B.

Example:
If a user requests to view transactions associated with the address 0xaa7a9ca87d3694b5755f213b5d04094b8d0f0a6f from block 9000000 to the current block, your application should be able to crawl and visualize all transaction data (addresses that have sent and received tokens from the address 0xaa7a9ca87d3694b5755f213b5d04094b8d0f0a6f, and how much ETH was used for a given transaction) in that period of time.
For bonus points:
Given a date in YYYY-MM-DD format, the program should return the exact value of ETH that was available on the given address at YYYY-MM-DD 00:00 UTC time.
Do the same task above to include tokens amounts (other than ETH)

Instructions
Use any technology you would like/are familiar with (language, database)
Please include instructions on how to run the project together with the code (so our team members can run it in one of our environments)
A list of officially available ethereum clients can be found here: http://ethdocs.org/en/latest/connecting-to-clients/. Other implementations can be found on the web. Additionally, you can utilize an API from a service such as https://etherscan.io/apis or register an account on infura.io service to access data from the blockchain directly.
See https://etherscan.io/ for an example of how data can be visualized.

Please, put your resolution to the private github repository and share it with marijakrivosic.


## Uses
- Java 21
- Gradle
- Spring Boot 
- Postgres
- Etherscan and Alchemy API use
- Scheduled calling of Etherscan API to get pseudo-latest block number
- Node (Express)
- Docker and docker-compose
- Two stage creation, usinga builder (JDK image) and runner (JRE image)
- API result caching
- Backend Logging


## How to run
- `git clone` this repository
- navigate to `/backend/api/src/resources`
- create `.env` file 
- write your Etherscan API key or use the one sent in the email
- write your Alchemy API key or use the one sent in the email
- use the following format:
  `ETHERSCAN_API_KEY=LIJEP_I_SUNCAN_DAN                              
    ALCHEMY_API_KEY=VJETROVIT_DAN_JE_ISTO_LIJEP`
- then navigate back to the root of the project
- `docker-compose up --build`
