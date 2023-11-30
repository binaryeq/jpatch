# data collection pipeline

- load project-kb data:
   - from [`<project-kb-repo>/statements`](https://github.com/SAP/project-kb/tree/vulnerability-data/statements)
   - only the ones that have "fixes" field (fix commits)
   - stats:
     - #statements: 1297
     - #statements with "artifacts": 617
     - #statements with "fixes": 1215
     - #statements with "artifacts" and "fixes": 535
   - notes:
     - "vulnerability_id" = CVE id

- load GHSA data:
  - only "github-reviewed" (from [`<ghsa-repo>/advisories/github-reviewed`](https://github.com/github/advisory-database/tree/main/advisories/github-reviewed))
  - stats:
    - #records: 3989
  - notes:
    - "aliases" = CVE id

- join GHSA and project-kb data:
  - join is on CVE id
  - stats:
    - #records: 666
    - #records with one "ghsa.affected" version range: 365
    - #records with one "kb.fixes.id" (fix commit branch): 481
    - #records with one "kb.fixes.commits" (fix commit): 385
    - #records with one fix commit and one affected range: 270

- continue with records with one fix commit and one affected range

- filter out records that report "last_affected" version instead of a range
  - stats:
    - #omitted records: 15
  - notes:
    - there is no clear and coherent semantic for "last_affected" field

- get version list of each GA from Maven
   - from `https://repo1.maven.org/maven2/<GA>/maven-metadata.xml` and `https://repo.jenkins-ci.org/releases/<GA>/maven-metadata.xml`
   -  stats:
      -  #errors: 4
      -  #GA: 167
      -  #versions: 14477

- get the latest vulnerable version of each package per CVE
  - we look up the fixed version from the version list and count the version right before it as the latest vulnerable version.
  - stats:
    - #records that their "kb.affected.fixed" version is not present in Maven: 47
    - #remaining records: 204

- get modified java files for each patch commit from repositories
  - stats:
    - #commit fetch errors: 5
    - #total number of modified files: 639
  - notes: 
    - 73 records do not have "source changes" tar file in project-kb. that's why we decided to get patches directly from their repositories

- filter out modified files that do not change any method
  - stats:
    - #discarded: 55

- get jar files
  - stats:
    - #jars from Maven Central: 264
    - #jars from Jenkins: 17
    - #modified files with successfully downloaded jar: 569

- extract corresponding class files of modified java files
  - stats:
    - #successfully found unique modified class files: 209
  - notes:
    - manual investigation:
      - 186 modified java files contain test classes
      - 28 cases of identical .class files
      - numerous cases of incorrect artifactId in GHSA (e.g. CVE-2019-10093)
      - some cases of incorrect version numbers in GHSA (e.g. CVE-2013-7285)

