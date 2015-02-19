## OLOPath.db

### Installation

Java VM should installed.

* Extract content of [OLOPath.db.zip](https://github.com/INTERCROSSING/OLOPath.db/blob/master/OLOPath.db.zip?raw=true).
* Add `OLOPath.db` directory to systems path. This step can be ommited the tool still be accessable by changing working directory to `OLOPath.db`: `cd OLOPath.db`.
* For OS X and Linux: make `olo` executable: `chmod +x olo`.
* Run `olo import all` to download and import data sources. This step takes several minutes.

### Usage

```
olo import all
```
Download and import to database all data sources UniprotKB, GeneSetDB and BioSystems.

```
olo cluster <database1,database2,...> -d0 <distance> -min <minSize> [-max <maxSize>] -o <file>
```
Cluster gene sets from specified databases with specified distance d0,
gene sets that smaller than minSize and larger than maxSize will be filtered. E.g.:
```
olo cluster BioSystems,GeneSetDB -d0 0.1 -min 10 -o genesets.txt
```

```
olo compare <database1> <database2> -d0 <distance> -min <minSize> [-max <maxSize>] -unique <uniqueFile> -common <commonFile>
```
Compare gene sets from database1 against database2 using specified distance d0,
gene sets from database1 that couldn't be mapped to any gene set from database2 written to uniqueFile
and to commonFile otherwise. E.g.:

```
olo compare GeneSetDB,BioSystems -d0 0.15 -min 10 -max 1000 -unique unique.txt -common common.txt
```

```
olo import UniprotKB
```
Download and import UniprotKB.

```
olo import GeneSetDB
```
Download and import GeneSetDB.

```
olo import BioSystems.
```
Download and import BioSystems.

```
olo database status.
```
Print status of the database.
