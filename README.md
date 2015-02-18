## OLOPath.db

### Installation

Java VM should installed.

* Extract content of [OLOPath.db.zip](???).
* Add `OLOPath.db` directory to systems path. This step can be ommited the tool still be accessable by changing working directory to
`OLOPath.db`.
* Run `olo import all` to download and import data sources. This step takes several minutes.

### Usage

```
olo import all
```
Download and import to database all data sources UniprotKB, GeneSetDB and BioSystems.


```
olo cluster BioSystems GeneSetDB -d0 <distance> -min <minSize> [-max <maxSize>] -o <file>
```

Cluster gene sets from BioSystems and GeneSetDB with specified distance d0,
gene sets that smaller than minSize and larger than maxSize will be filtered.

```
olo cluster GeneSetDB -d0 <distance> -min <minSize> [-max <maxSize>] -o <file>
```

Cluster gene sets from GeneSetDB with specified distance d0,
gene sets that smaller than minSize and larger than maxSize will be filtered.

```
olo cluster BioSystems -d0 <distance> -min <minSize> [-max <maxSize>] -o <file>
```

Cluster gene sets from GeneSetDB with specified distance d0,
gene sets that smaller than minSize and larger than maxSize will be filtered.

```
olo compare BioSystems GeneSetDB -d0 <distance> -min <minSize> [-max <maxSize>] -unique <uniqueFile> -common <commonFile>
```

Map gene sets from BioSystems with GeneSetDB using specified distance d0,
gene sets from BioSystems that couldn't be mapped to any gene set from GeneSetDB written to uniqueFile
and to commonFile otherwise.

```
olo compare GeneSetDB BioSystems -d0 <distance> -min <minSize> [-max <maxSize>] -unique <uniqueFile> -common <commonFile>
```

Comapre gene sets from GeneSetDB with BioSystems using specified distance d0,
gene sets from GeneSetDB that couldn't be mapped to any gene set from GeneSetDB written to uniqueFile
and to commonFile otherwise.


```
olo database status
```

Print status of database.
