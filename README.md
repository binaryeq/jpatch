# JPatch


1. collect CVEs recorded in the [sap-project-kb](https://github.com/SAP/project-kb/) project, this will have patch commits, incl [the actual sources that have been changed](https://github.com/SAP/project-kb/blob/vulnerability-data/statements/CVE-2016-4464/statement.yaml)
2. then look up version ranges, using [GHSA API](https://docs.github.com/en/rest/security-advisories/global-advisories?apiVersion=2022-11-28#get-a-global-security-advisory) (or if this does not work, snyk or NVD). Open: need to map CVEs to GHSA id for query. Possibilities:
    1. Clone [https://github.com/github/advisory-database/](https://github.com/github/advisory-database/) and then get info from JSON entries , look for alias fields.
    2. Use [GitHub's GHSA REST API for listing advisories](https://docs.github.com/en/rest/security-advisories/global-advisories?apiVersion=2022-11-28#list-global-security-advisories) with the `cve_id​` URL parameter to find the GHSA for a given CVE
       - E.g.: `curl -i -H "Accept: application/vnd.github+json" -H "X-GitHub-Api-Version: 2022-11-28" "https://api.github.com/advisories?cve_id=CVE-2015-6420"`
       - Note the JSON format is **not identical** to the format of the raw GHSA JSON in the repo (also it contains other info like credits)
       - I found [adding a PAT](https://github.com/settings/tokens?type=beta) (it doesn't need any account or repo permissions) and then supplying it to `curl`​ with `-H "Authorization: Bearer github_pat_<REST_OF_YOUR_PAT>"` increased the rate limit from 60 requests per hour to 5000 per hour
3. this would give us vulnerable and fixed version ranges, so we can use this to locate the built jars in Maven Central. The advantage of this approach is that we avoid building which is known to be tricky at scale (todo: try Crista Lopes' work for supporting ref).


## Issues With Using Prebuilt Jars

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


## Challenges 

### Inner Classes

1. Named inner classes are separated out into separate records (rows within the CSV file).
2. Anymymous inner classes are considered to be part of their parents

### Dataset Start & Age

SAP-KB and GHSA only overlap for a few years: [SAP-KB: 2005-2019](https://github.com/SAP/project-kb/tree/vulnerability-data/statements), [GHSA starts in 2017](https://github.com/github/advisory-database/tree/main/advisories/github-reviewed). Start with overlap, this is still a good size, then discuss later whether there is an easy way to extend this. 


   

 
