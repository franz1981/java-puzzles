These are a collection of experiments (mostly using JMH), stressing specific JVM mechanisms 
eg global safepoint, tlab allocation, super-word optimization.

## JMH Benchmark GitHub Action

This repository includes manual JMH benchmark execution using GitHub Actions with [@benchmark-action/github-action-benchmark](https://github.com/benchmark-action/github-action-benchmark).

**Features:**
- 🎯 Manual benchmark execution with specific benchmark selection
- ⚙️ Configurable Java versions (11, 17, 21, 23)
- 🖥️ macOS M1/M2 support for ARM architecture testing
- 📊 Performance regression detection and alerts
- 📈 Historical performance tracking

**Usage:**
1. Go to Actions → JMH Benchmark → Run workflow
2. Specify benchmark name/pattern (required): e.g., `LockCoarsening`, `.*String.*`, `ToLowerCaseAndDotted.toLowerCaseAndDottedBuilderReuse`
3. Select Java version (optional, defaults to 21)

See [docs/JMH_BENCHMARK_ACTION.md](docs/JMH_BENCHMARK_ACTION.md) for detailed documentation. 