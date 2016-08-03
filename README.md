# LynxDB server

LynxDB is a time-series database built on-top of Cassandra by guys with nothing else to do.

It provides multi-user and multi virtual-host support as we designed it to be used as a TSDB-aaS.

## Whats it currently does:
* OpenTSDB-like API & features (based on v 2.0, only put, query, suggest)
* Multi-user support via basic authentication
* Multi-vhost support (user are created inside a specific vhost)


## What's being worked on:
* Per-vhost stats/accounting/quotas
* Per-user stats/accounting/quotas
* User-based auto-tagging (ex: tag [user = 123] added to all entries pushed by user 123)
* Request optimizations
* Grpc/protobuf api
