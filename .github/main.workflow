workflow "Run Tests" {
  resolves = ["Run Gradle tests"]
  on = "push"
}

action "Run Gradle tests" {
  uses = "docker://gradle:jdk12"
  args = "gradle test"
}
