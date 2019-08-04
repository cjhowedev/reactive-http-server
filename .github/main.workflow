workflow "Run Tests" {
  on = "push"
  resolves = ["Run Gradle tests"]
}

action "Run Gradle tests" {
  uses = "docker://gradle"
  args = "test"
}
