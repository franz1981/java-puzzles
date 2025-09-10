These are a collection of experiments (mostly using JMH), stressing specific JVM mechanisms 
eg global safepoint, tlab allocation, super-word optimization.

## JMH Benchmark GitHub Action

This repository now includes automated JMH benchmark execution using GitHub Actions with [@benchmark-action/github-action-benchmark](https://github.com/benchmark-action/github-action-benchmark).

**Features:**
- 🚀 Automatic benchmark execution on PRs and pushes
- ⚙️ Configurable Java versions (11, 17, 21, 23)
- 🖥️ Multi-platform support (Ubuntu Linux + macOS M1/M2)
- 📊 Performance regression detection and alerts
- 📈 Historical performance tracking

See [docs/JMH_BENCHMARK_ACTION.md](docs/JMH_BENCHMARK_ACTION.md) for detailed documentation. 