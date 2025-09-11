# Quick Start Guide for JMH Benchmark Workflow

## Automatic Execution

The JMH benchmark workflow runs automatically when:
- Creating or updating a Pull Request to `main` or `master` branch
- Pushing directly to `main` or `master` branch

## Manual Execution

To run benchmarks manually with custom settings:

1. Go to the **Actions** tab in your GitHub repository
2. Select **JMH Benchmark** workflow
3. Click **Run workflow**
4. Configure options:
   - **Java version**: Select from 11, 17, 21, or 23 (default: 21)
   - **Benchmark pattern**: Enter regex pattern (default: `.*` for all benchmarks)

## Example Patterns

Run specific benchmarks by pattern:
- `AsciiCopy.*` - Run all AsciiCopy benchmarks
- `.*String.*` - Run all string-related benchmarks  
- `.*concurrent.*` - Run all concurrency benchmarks
- `AsciiCopy.asciiStringBytesCopy` - Run specific benchmark

## Results

- **Performance tracking**: Results are automatically compared to baseline
- **PR comments**: Alerts are posted if performance degrades by >150%
- **Artifacts**: Raw JSON results are uploaded for detailed analysis
- **Multi-platform**: Results are generated for both Ubuntu Linux and macOS (M1/M2)

## Supported Java Versions

- **Java 11**: Compatible benchmarks only (excludes Loom and Foreign Memory features)
- **Java 17**: Most benchmarks (excludes some Java 21+ features)
- **Java 21**: Full benchmark suite with preview features (default)
- **Java 23**: Latest features

## Example Workflow Usage

### Via GitHub UI
1. Navigate to Actions → JMH Benchmark → Run workflow
2. Select Java 21 and pattern `.*Copy.*`
3. View results in the workflow run

### Via Pull Request
1. Create PR with benchmark changes
2. Workflow runs automatically
3. Performance comparison posted as PR comment

### Interpreting Results
- **ops/ms**: Operations per millisecond (higher is better)
- **Alert**: Performance degraded >150% from baseline
- **Artifacts**: Download JSON files for detailed analysis