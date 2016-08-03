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

## Contributing

Source code for LynxDB server is MIT licensed. We encourage users to make contributions on [Github](https://github.com/lynxdb/server).

If you find bugs or documentation mistakes, please check [open issues](https://github.com/lynxdb/server/issues) before [creating a new issue](https://github.com/lynxdb/server/issues/new). Please be specific and give a detailed description of the issue. Explain the steps to reproduce the problem. If you're able to fix the issue yourself, please help the community by forking the repository and submitting a pull request with your fix.

For contributing a feature, please open an issue that explains what you're working on. Work in your own fork of the repository and submit a pull request when you're done.

If you want to contribute, but don't know where to start, you could have a look at issues with the label [*help wanted*](https://github.com/lynxdb/server/labels/help%20wanted) or [*difficulty/easy*](https://github.com/lynxdb/server/labels/difficulty%2Feasy).

## License

Source code for The Things Network is released under the MIT License, which can be found in the [LICENSE](LICENSE) file. A list of authors can be found in the [AUTHORS](AUTHORS) file.
