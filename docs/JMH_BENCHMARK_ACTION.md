# JMH Benchmark GitHub Action

This repository includes a GitHub Actions workflow that integrates JMH (Java Microbenchmark Harness) benchmarks with [@benchmark-action/github-action-benchmark](https://github.com/benchmark-action/github-action-benchmark).

## Features

- ✅ **Automatic benchmark execution** on PRs and pushes to main/master branches
- ✅ **Configurable Java versions** (11, 17, 21, 23) 
- ✅ **Multi-platform support** (Ubuntu Linux and macOS including M1/M2)
- ✅ **Flexible benchmark selection** via regex patterns
- ✅ **Performance regression detection** with customizable alert thresholds
- ✅ **Results storage and visualization** through github-action-benchmark
- ✅ **Artifact uploads** for detailed analysis

## Workflow Configuration

The workflow is defined in `.github/workflows/jmh-benchmark.yml` and can be triggered in three ways:

### 1. Automatic Triggers
- **Pull Requests**: Benchmarks run automatically on PRs targeting `main` or `master`
- **Push to main/master**: Benchmarks run on direct pushes to main branches

### 2. Manual Trigger (workflow_dispatch)
You can manually trigger the workflow with custom parameters:

- **Java Version**: Choose from Java 11, 17, 21, or 23 (default: 21)
- **Benchmark Pattern**: Regex pattern to select which benchmarks to run (default: `.*` for all)

## Usage Examples

### Running All Benchmarks
The workflow runs all benchmarks by default when triggered automatically or manually without parameters.

### Running Specific Benchmarks
When manually triggering the workflow, you can specify a regex pattern to run only certain benchmarks:

- `.*String.*` - Run all string-related benchmarks
- `.*Copy.*` - Run all copy-related benchmarks  
- `AsciiCopy` - Run only the AsciiCopy benchmark

### Java Version Selection
The workflow defaults to Java 21 (as configured in the project), but you can manually select:
- Java 11 for older compatibility testing
- Java 17 for LTS compatibility
- Java 21 for latest features (default)
- Java 23 for cutting-edge features

## Benchmark Results

### Performance Tracking
- Results are processed by `@benchmark-action/github-action-benchmark`
- Performance regressions are detected (threshold: 150% of baseline)
- Comments are automatically posted on PRs when alerts are triggered
- Historical performance data is stored for trend analysis

### Artifacts
- Benchmark results are uploaded as workflow artifacts
- Separate artifacts for each OS and Java version combination
- Results are in JSON format compatible with JMH output

## Platform Support

### Ubuntu Linux (`ubuntu-latest`)
- Standard GitHub-hosted runner
- Good for general performance testing
- Consistent environment for baseline measurements

### macOS (`macos-latest`) 
- Includes support for M1/M2 Apple Silicon
- Tests performance on ARM architecture
- Important for cross-platform performance validation

## Customization

### Modifying JMH Parameters
The workflow uses these default JMH parameters:
```bash
java -jar target/benchmark.jar \
  -f 1 \        # 1 fork
  -wi 3 \       # 3 warmup iterations  
  -i 5 \        # 5 measurement iterations
  -tu ms \      # Time unit: milliseconds
  -rf json \    # Output format: JSON
  -rff results.json  # Results file
```

To modify these parameters, edit the "Run JMH Benchmarks" step in the workflow file.

### Alert Threshold
The performance regression alert threshold is set to 150%. To modify:

```yaml
alert-threshold: '200%'  # Change to 200% threshold
```

### Additional Java Versions
To add support for other Java versions, update the workflow options:

```yaml
options:
  - '11'
  - '17' 
  - '21'
  - '23'
  - '24'  # Add Java 24
```

## Requirements

### Repository Setup
- Java project with Maven build configuration
- JMH dependencies in `pom.xml` (already configured)
- Maven Shade plugin to create executable JAR (already configured)

### GitHub Permissions
- `GITHUB_TOKEN` is automatically provided by GitHub Actions
- No additional setup required for basic functionality

## Integration with Existing Projects

To add this JMH benchmark workflow to your own Java project:

1. **Copy the workflow file** to `.github/workflows/jmh-benchmark.yml`
2. **Update pom.xml** with JMH dependencies and shade plugin (see this project's pom.xml)
3. **Ensure JMH annotations** are used in your benchmark classes
4. **Customize** Java versions and parameters as needed

## Example Benchmark Class

```java
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class MyBenchmark {
    
    @Benchmark
    public void testMethod() {
        // Your benchmark code here
    }
}
```

## Troubleshooting

### Build Failures
- Ensure the correct Java version is available
- Check that Maven dependencies are properly declared
- Verify Maven Shade plugin configuration

### Benchmark Failures  
- Check that benchmark classes are properly annotated
- Ensure benchmark methods are public
- Verify JMH output format compatibility

### Performance Alerts
- Review performance regression threshold settings
- Check for environmental factors affecting performance
- Consider platform-specific performance characteristics