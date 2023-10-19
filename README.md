# JPatch


1. collect CVEs recorded in the [SAP-kb](https://github.com/SAP/project-kb/) project, this will have patch commits, incl [the actual sources that have been changed](https://github.com/SAP/project-kb/blob/vulnerability-data/statements/CVE-2016-4464/statement.yaml)
2. then look up version ranges, using [GHSA API](https://docs.github.com/en/rest/security-advisories/global-advisories?apiVersion=2022-11-28#get-a-global-security-advisory) (or if this does not work, snyk or NVD). Open: need to map CVEs to GHSA id for query.
3. this would give us vulnerable and fixed version ranges, so we can use this to locate the built jars in Maven Central. The advantage of this approach is that we avoid building which is known to be tricky at scale (todo: look up Crista Lopez' work for supporting ref).


## Issues With Using Prebuild Jars

1. the release version ranges between vulnerable and fixed version may be much larger than the commit
2. issue 1 -- more classes could be touched -- can be mitigated by using project-kb data that has the classes modified in the security patch
3. issue 2 -- the classes modified in the security patch might have more changes applied to them in other commits between the version -- nothing we can do here other than argueing that this wont have a major impact
