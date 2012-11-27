A price encryption/decryption library for OpenX server side real-time bidding. 

## DESCRIPTION
A price encryption/decryption library for OpenX server side real-time bidding. 

## Usage

### Hive
````sh
hive  select reflect( 'org.openx.market.ssrtb.crypter.PriceDecrypt', 'decodeDecrypt', rtt_winning_price, 'your_encryptKey', 'your_integrityKey' ) ... 
````
