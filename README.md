# JPatch


1. collect CVEs recorded in the [SAP-kb](https://github.com/SAP/project-kb/) project, this will have patch commits, incl [the actual sources that have been changed](https://github.com/SAP/project-kb/blob/vulnerability-data/statements/CVE-2016-4464/statement.yaml)
2. then look up version ranges, using [GHSA API](https://docs.github.com/en/rest/security-advisories/global-advisories?apiVersion=2022-11-28#get-a-global-security-advisory) (or if this does not work, snyk or NVD). Open: need to map CVEs to GHSA id for query. Can clone https://github.com/github/advisory-database/ and the get info from JSON entries , alias field.
3. this would give us vulnerable and fixed version ranges, so we can use this to locate the built jars in Maven Central. The advantage of this approach is that we avoid building which is known to be tricky at scale (todo: look up Crista Lopez' work for supporting ref).


## Issues With Using Prebuild Jars

1. the release version ranges between vulnerable and fixed version may be much larger than the commit
2. issue 1 -- more classes could be touched -- can be mitigated by using project-kb data that has the classes modified in the security patch
3. issue 2 -- the classes modified in the security patch might have more changes applied to them in other commits between the version -- nothing we can do here other than argueing that this wont have a major impact

## Result Schema  (Format is CSV - tab separated) -- Columns

1. SAP-KB entry URL
2. GHSA entry URL
3. CVE
4. commit URL
5. latest vulnerable version GAV
6. fixed version GAV
7. source paths affected in commit (from sap-kb)
8. relative path to  latest vulnerable version jar (downloaded from Maven central)
9. fixed version jar (downloaded from Maven central)
10. path of affected .class file relative to latest vulnerable version jar
11. name of affected .class file


## Inner Classes

1. Named inner classes are separated out into separate records (rows within the CSV file).
2. Anymymous inner classes are considered to be parts of their parents

 
