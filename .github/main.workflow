workflow "Continuous integration" {
  resolves = ["Unit tests", "Code coverage"]
  on = "push"
}

action "Unit tests" {
  uses = "docker://gradle:jdk12"
  args = "gradle test"
}

action "Code coverage" {
  uses = "docker://gradle:jdk12"
  args = "gradle jacocoTestCoverageVerification"
}

