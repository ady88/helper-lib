# GitHub Copilot Comprehensive Training Exercises - Session 2: Agent-Based Development Workflow (Java)

Welcome to Session 2! You'll now dive into advanced agent-based development workflows. These exercises implement a structured approach focusing on **multi-agent collaboration** and **complex feature implementation**.

> **💡 About Custom Chatmodes**
>
> Other IDEs will eventually support custom chatmodes similar to VS Code, allowing you to save and reuse agent configurations. For now, we'll manually prime agents with specific roles and instructions using reusable prompt files. This approach teaches you valuable prompt engineering skills and gives you full control over agent behavior.
>
> **What this means for you:**
> - You'll use **agent priming prompts** to define roles (Lead Developer, Implementer, QA Agent)
> - You'll store reusable prompts in `.github/prompts/` as `.prompt.md` files
> - For Visual Studio, you'll reference them using `#prompt:custom_prompt_name` in Copilot Chat
> - For JetBrains, you'll reference them using `/custom_prompt_name` in Copilot Chat
> - Each new chat session requires manual role definition

## Model Recommendations

Different agents work best with different AI models:

- **Lead Developer**: Claude Sonnet 4/4.5 or GPT-4 (better at detailed planning and research)
- **Implementer**: Claude Sonnet 4/4.5 (superior code generation and precision)
- **Deep reasoning and debugging**: Gemini 2.5 Pro or o1

**Always verify before running a prompt:**
1. Check the model selector shows your preferred model for that task
2. Manually switch models if needed
3. Keep track of which role you're using in each chat session

## Exercise 1: Complete Weather Data Persistence System Implementation

### Scenario: Multi-Agent Epic Development

You've been tasked with adding a complete weather data persistence system to the Simple Weather CLI Application. This comprehensive exercise demonstrates the full agent-based development workflow from requirements analysis through implementation and completion. This will require implementing data storage, caching mechanisms, historical weather data tracking, and configuration management for different storage backends.

### Phase 1: Multi-Agent Feature Planning

#### Part 1.1: Requirements Analysis with Ask Agent

1. **Create Context Understanding**
   - Open a new Copilot Chat and set the context: "I am analyzing requirements for a weather data persistence system"
   - Ask: `@workspace Analyze the current architecture of the Weather CLI application. How would adding data persistence impact the existing service layer and data model?`
   - Follow up: `What are the main challenges and considerations for adding persistent storage to this weather application?`
   - Request: `Identify all Java files that would need modification and new files that need creation for data persistence`

2. **Storage and Performance Analysis**
   - Ask: `From a performance and storage perspective, what caching and persistence patterns should I implement for weather data?`
   - Request: `How should weather data flow through the existing service architecture with added persistence?`
   - Analyze: `What database or storage solutions would be appropriate for this Java application (H2, SQLite, file-based)?`

3. **Integration Impact Assessment**
   - Ask: `How will data persistence affect existing classes like WeatherService, WeatherData, and OpenWeatherMapClient?`
   - Request: `What backwards compatibility considerations do I need for existing weather data retrieval?`
   - Evaluate: `What Java libraries or frameworks would be recommended for data access (JDBC, JPA, or file I/O)?`

**Deliverable:** Create a `REQUIREMENT-ANALYSIS.md` file documenting all findings, challenges, and recommendations.

#### Part 1.2: Lead Developer Planning Agent

**Understanding Agent Priming:**
Since other IDEs than VS Code doesn't yet have fully automatic custom chatmodes, you'll manually prime the agent with a specific role. This means starting each chat session by telling Copilot what role to play and what rules to follow.

1. **Create the Lead Developer Prompt File**
   - Create `.github/prompts/custom_lead-plan.prompt.md` with the following content:

```markdown
You are a Lead Java Developer responsible for architectural decisions, code reviews, and ensuring best practices.

Your task is to create a detailed implementation plan based on the provided requirements analysis or test analysis.

Follow this process:
1. Read and understand the requirements analysis or test analysis document
2. Break down the work into small, sequential, numbered tasks
3. Each task should be completable in one development session
4. Create the following deliverables:
   - `docs/epic_[name]/plans/IMPLEMENTATION_PLAN.md` - Overall strategy and approach
   - `docs/epic_[name]/plans/DECISION_LOG.md` - Key architectural decisions
   - `docs/epic_[name]/tasks/01_[task_name].md` - First task with clear goals and acceptance criteria
   - `docs/epic_[name]/tasks/02_[task_name].md` - Second task, and so on
   - `docs/epic_[name]/MANIFEST.md` - List of all generated files

Task File Format:
- Title: Clear, actionable task name
- Goal: What this task accomplishes
- Context: Files and components involved
- Acceptance Criteria: How to verify completion
- Implementation Notes: Technical guidance
- Use absolute paths from project root (not placeholders)

Plan Requirements:
- Focus on Java 11+, Maven build system, and Java best practices
- Each task must be independent (no blocking dependencies)
- Number tasks sequentially (01, 02, 03...)
- Keep tasks small and focused
- Follow existing project structure in src/main/java/com/weather/app/
- Use proper package structure and naming conventions
- Consider integration with existing WeatherService, WeatherData, and OpenWeatherMapClient classes

Epic name will be provided. Generate all files with proper structure.
```

2. **Use the Lead Developer Prompt**
   - Start a **new Copilot Chat session**
   - Reference the prompt file: `#prompt:custom_lead-plan`
      - Or For JetBrains: `/custom_lead-plan`
   - Add your requirements file: `#file:REQUIREMENT-ANALYSIS.md`
   - Provide the epic name: "The epic name is 'weather_data_persistence'. Create the implementation plan."

3. **Review the Generated Plan**
   - The agent will create a new epic in `docs/epic_weather_data_persistence/` containing:
     - `plans/IMPLEMENTATION_PLAN.md`: The overall strategy
     - `plans/DECISION_LOG.md`: Key decisions and rationale
     - `tasks/01_[name].md`, `tasks/02_[name].md`, etc.: Sequenced tasks
     - `MANIFEST.md`: A manifest of all generated files

   **Your responsibility:**
   - Read each task file to ensure it makes sense
   - Verify tasks are small enough (each should be completable in one session)
   - Check that file paths use project root (`/`) not placeholders
   - Ensure tasks follow Java and Maven conventions

**Deliverable:**
   - Output files in `docs/epic_weather_data_persistence/`:
     - `plans/IMPLEMENTATION_PLAN.md`
     - `plans/DECISION_LOG.md`
     - `tasks/01_[name].md`, `tasks/02_[name].md`, etc.
     - `MANIFEST.md`

#### Part 1.3: Experimenting with Custom Planning Prompts

Instead of using the structured prompt file, you can experiment with generating the plan using your own custom prompts. This is a great way to practice prompt engineering and compare outputs.

1. **Start a new chat session** with your preferred model (e.g., Claude Sonnet 4, GPT-4)
2. **Provide Context**: Add `#file:REQUIREMENT-ANALYSIS.md`
3. **Craft Your Own Prompt**: Try variations like:
   > "Based on the attached requirements analysis, create a detailed implementation plan for adding weather data persistence to this Java Weather CLI application. Break it into 5-7 numbered, sequential task files. Each task should focus on a specific component (data models, repositories, services, caching, etc.). Use Java 11+ and Maven best practices. Generate a MANIFEST.md listing all files you create."

4. **Compare Results**: 
   - How does your custom prompt compare to the structured prompt?
   - Which produces clearer task definitions?
   - What prompt patterns work best for planning?

### Phase 2: Collaborative Implementation Workflow

#### Part 2.1: Create the Implementer Prompt File

1. **Create `.github/prompts/custom_implement.prompt.md`:**

```markdown
You are a Java Implementer responsible for executing tasks with precision and quality.

Your role:
- Read and understand the task specification
- Implement code following Java 11+, Maven, and modern Java best practices
- Write clean, maintainable, well-documented code
- Use proper error handling and validation patterns
- Follow the existing project structure and conventions

Process:
1. Read the task file and summarize what you will do
2. List all files you will create or modify (use absolute paths from project root)
3. Ask for approval before proceeding
4. After approval, implement the task step by step
5. Run Maven build and checkstyle validation on modified files
6. Execute tests if applicable
7. Report completion status

Code Standards:
- PascalCase for class names
- camelCase for method names and properties
- Use proper packages following com.weather.app structure
- Add Javadoc comments for public classes and methods
- Use dependency injection patterns where appropriate
- Follow existing patterns in WeatherService, WeatherData, and ConfigUtil
- Implement proper exception handling using WeatherApiException and custom exceptions
- Use java.util.logging.Logger for logging
- Follow checkstyle.xml rules for code formatting

File Structure:
- Main classes in src/main/java/com/weather/app/
- Test classes in src/test/java/com/weather/app/
- Resources in src/main/resources/
- Configuration files follow existing patterns (logging.properties, etc.)

Maven Integration:
- Run `mvn clean compile` after code changes
- Run `mvn checkstyle:check` for code style validation
- Run `mvn test` for test execution
- Address any build or checkstyle failures

If verification fails:
- Explain what went wrong
- Propose a solution
- Ask for guidance if blocked

Always think through the task before implementing. Quality over speed.
```

#### Part 2.2: Implement the First Task

1. **Start a new Copilot Chat session**
2. **Prime the agent**: Reference `#prompt:custom_implement`
   - Or For JetBrains: `/custom_implement`
3. **Add task context**: Add `#file:docs/epic_weather_data_persistence/tasks/01_[task_name].md`
4. **Request implementation**: "Implement this task following the process defined in the prompt."

**The Implementer will:**
- Read and summarize what it plans to do
- List all files it will create/modify
- Ask for your approval to proceed

**Your responsibility:**
- Review the implementation plan
- Confirm it matches the task specification
- Check that it follows Java and Maven conventions
- Approve with "yes" or request clarification

**Once approved, the Implementer will:**
- Execute the task step by step
- Run Maven build and checkstyle validation
- Execute tests if applicable
- Report completion status

#### Part 2.3: Handle Implementation Issues

**If the task succeeds:**
- Review the code changes
- Test manually by running `mvn clean package` and `java -jar target/weather-app-1.0-SNAPSHOT-jar-with-dependencies.jar [city]`
- Move to the next task (repeat Part 2.2 with `02_[task_name].md`)

**If verification fails:**
- Read the Implementer's explanation
- Minor issues: Let it proceed if non-critical items failed
- Major issues: Ask the agent to propose solutions

**If the Implementer gets blocked:**
The agent will present what went wrong and propose solutions.

You can:
- Approve a proposed solution
- Provide an alternative approach
- Modify the task specification
- Go back to Lead Developer for task revision

#### Part 2.4: Complete Remaining Tasks

Repeat Part 2.2 for each task file in sequence (02, 03, etc.) until all tasks in the epic are complete.

**Important:** Each task should be run in a fresh chat session with the Implementer role primed using `#prompt:custom_implement` or for JetBrains: `/custom_implement`

#### Part 2.5: Experimenting with Custom Implementation Prompts

Want to try a different implementation approach? Create your own prompt!

1. **Start a new chat session**
2. **Add task context**: `#file:docs/epic_weather_data_persistence/tasks/01_[task_name].md`
3. **Craft your prompt**: Try variations like:
   > "Act as a senior Java developer. Implement the attached task using Java 11+ and Maven. List all files you'll modify, explain your approach, and then implement it step by step. Follow existing project patterns in WeatherService and WeatherData. Use proper logging and exception handling. Test your implementation with Maven."

4. **Apply changes**: Copy code blocks and apply them to your workspace

This hands-on approach helps you understand how to guide an agent through complex tasks.

#### Part 2.6: Complete the Epic

After the last task succeeds:

1. **Create `.github/prompts/custom_report-to-lead.prompt.md`:**

```markdown
You are a Java Implementer reporting completion of an epic to the Lead Developer.

Your task:
- Review the implementation plan and manifest
- Summarize all work completed
- Note any deviations from the original plan
- Highlight challenges encountered and how they were resolved
- Provide recommendations for future epics
- List all Java files created or modified

Generate a completion report in markdown format with:
- Epic name and summary
- Completion status
- Implementation summary
- Deviations and rationale
- Lessons learned
- Recommendations

Be concise but thorough. Focus on architectural decisions and code quality.
```

2. **Generate the Completion Report**
   - Start a **new chat session** or continue in Implementer mode
   - Reference: `#prompt:custom_report-to-lead`
     - Or For JetBrains: `/custom_report-to-lead`
   - Add context: `#file:docs/epic_weather_data_persistence/plans/IMPLEMENTATION_PLAN.md` and `#file:docs/epic_weather_data_persistence/MANIFEST.md`
   - Request: "Generate the completion report."

The agent generates a completion report with:
- Summary of work completed
- Any deviations from plan
- Recommendations for future epics

## Exercise 2: Comprehensive Testing and QA

### Scenario: Agent-Driven Quality Assurance

The weather data persistence system from Exercise 1 is feature-complete, but it hasn't been tested! Your task is to use a QA-focused agent workflow to create and implement a comprehensive test suite, ensuring the new features are robust, secure, and bug-free.

### Phase 1: Test Strategy and Planning

#### Part 1.1: Test Analysis with a QA Agent

1. **Create `.github/prompts/custom_qa-analysis.prompt.md`:**

```markdown
You are a QA Engineer specializing in Java testing and quality assurance.

Your task:
- Analyze recently implemented features for testability
- Identify critical code paths requiring testing
- Generate comprehensive test case lists
- Recommend testing tools and frameworks
- Identify potential vulnerabilities and edge cases

Focus areas:
- Unit tests for business logic (services, data models, utilities)
- Integration tests for persistence layer and external API calls
- Edge cases and error handling
- Configuration and environment validation tests
- Data validation and transformation tests

Deliverables:
- List of test cases (unit, integration, end-to-end)
- Security and reliability checklist
- Testing framework recommendations (JUnit 5, Mockito, etc.)
- Setup and configuration guidance

Use JUnit 5, Mockito, Maven Surefire, and Java testing best practices for Java 11+.
```

2. **Analyze the Feature Implementation**
   - Open a new Copilot Chat session
   - Reference: `#prompt:custom_qa-analysis`
     - Or For JetBrains: `/custom_qa-analysis`
   - Ask: `@workspace Based on the recently added weather data persistence system, analyze what needs testing.`
   - Follow up: `Generate a comprehensive list of test cases covering unit, integration, and edge case scenarios.`
   - Request: `What testing frameworks and setup do we need for this Java 11+ Maven project?`

**Deliverable:** Create a `TEST-ANALYSIS.md` file documenting the test cases, edge cases, and setup plan.

#### Part 1.2: Test Plan Generation with Lead Developer

1. **Generate Test Implementation Plan**
   - Start a **new Copilot Chat session**
   - Reference: `#prompt:custom_lead-plan`
        - Or For JetBrains: `/custom_lead-plan`
   - Add context: `#file:TEST-ANALYSIS.md`
   - Request: "Create a detailed test implementation plan. The epic name is 'weather_persistence_testing'. Focus on Java 11+, JUnit 5, Mockito, and Maven testing best practices."

2. **Review the Generated Plan**
   - The agent creates `docs/epic_weather_persistence_testing/` containing:
     - `plans/IMPLEMENTATION_PLAN.md`: Testing strategy
     - `tasks/01_[name].md`, `tasks/02_[name].md`, etc.: Sequential test implementation tasks
     - `MANIFEST.md`: Manifest of generated files
   - Verify tasks are logical, sequential, and appropriately sized

#### Part 1.3: Experimenting with Custom Test Planning

Try creating the test plan with your own prompt:

1. **Start a new chat session**
2. **Add context**: `#file:TEST-ANALYSIS.md`
3. **Custom prompt example**:
   > "Based on the attached test analysis, create a step-by-step test implementation plan for the 'weather_persistence_testing' epic. Break into numbered task files: setup test infrastructure, unit tests for persistence classes, integration tests for WeatherService, edge case tests, etc. Use JUnit 5, Mockito, and Maven patterns. Generate MANIFEST.md."

4. **Create files manually** based on the output
5. **Compare** with the structured prompt approach

### Phase 2: Test Implementation and Debugging

#### Part 2.1: Implement Test Tasks

1. **Execute Tasks with the Implementer**
   - For each task file (starting with `01_...`), start a **new chat session**
   - Reference: `#prompt:custom_implement`
        - Or For JetBrains: `/custom_implement`
   - Add task: `#file:docs/epic_weather_persistence_testing/tasks/01_[task_name].md`
   - Request: "Implement this task."
   - Review the plan and approve with "yes"
   - The agent will write test files, configuration, and helper code

#### Part 2.2: Experimenting with Custom Test Implementation

Try implementing tests with custom prompts:

1. **Start a new chat session**
2. **Add task**: `#file:docs/epic_weather_persistence_testing/tasks/01_[task_name].md`
3. **Custom prompt**:
   > "Based on the attached task, generate the necessary JUnit 5 test code for Java 11+. Use proper test structure, follow AAA pattern (Arrange-Act-Assert), use Mockito for mocking, and include proper test naming conventions. List all files you'll create or modify before implementing."

4. **Apply changes manually** from the agent's response

#### Part 2.3: Running Tests and Fixing Bugs

This is the core of the QA workflow.

1. **Run the Newly Created Tests**
   - After creating test files, run them from the terminal:
     - Single test class: `mvn test -Dtest=WeatherDataRepositoryTest`
     - Entire test suite: `mvn test`
     - Specific test method: `mvn test -Dtest=WeatherServiceTest#testCacheWeatherData`

2. **If Tests Pass:**
   - Excellent! Move to the next task in sequence
   - Commit your changes: `git add . && git commit -m "Complete task: [task_name]"`

3. **If Tests Fail (Bug Found):**
   - Start a **new chat session**
   - Paste the full error output into the chat
   - Ask: `@workspace This Java test is failing with the error below. Analyze the relevant Java code and the test to identify the bug. Propose a fix using Java best practices and proper exception handling.`
   - Include the error output in your message
   - Review the agent's analysis and proposed fix
   - Apply the fix, re-run tests to confirm they pass
   - Commit the fix: `git add . && git commit -m "Fix: [description]"`

4. **Create `.github/prompts/custom_debug-test.prompt.md` for Systematic Debugging:**

```markdown
You are a Java Debugging Specialist focused on test failures and quality issues.

Your task:
- Analyze Maven test failure output and stack traces
- Identify root causes (logic errors, configuration issues, test setup problems)
- Propose targeted fixes following Java best practices
- Consider Maven build configuration, dependency management, and classpath issues

Process:
1. Read the test failure output carefully
2. Identify the failing test and what it was testing
3. Analyze the relevant production code
4. Determine the root cause
5. Propose a specific fix with code examples
6. Explain why the fix resolves the issue

Common Java test issues:
- Maven dependency conflicts or missing test dependencies
- Classpath issues and resource loading problems
- Mock configuration problems (Mockito setup)
- Exception handling in tests (expected vs actual exceptions)
- Configuration loading issues (properties files, environment variables)
- Threading or timing issues in tests
- File I/O and resource management problems

Provide clear, actionable fixes with proper error handling and Java best practices.
```

5. **Use the Debug Prompt for Systematic Fixing:**
   - Start a new chat session
   - Reference: `#prompt:custom_debug-test`
    - Or For JetBrains: `/custom_debug-test`
   - Add failing test file: `#file:src/test/java/com/weather/app/WeatherDataRepositoryTest.java`
   - Paste error output and request: "Analyze this test failure and propose a fix."

#### Part 2.4: Complete the Test Suite

- Repeat the implement-run-fix cycle for all tasks in `docs/epic_weather_persistence_testing/tasks/`
- Ensure all tests pass before marking the epic complete
- Run the full suite: `mvn test` to verify everything works together
- Run checkstyle: `mvn checkstyle:check` to ensure code quality

#### Part 2.5: Generate Test Completion Report

1. **Complete the Testing Epic**
   - Start a **new chat session**
   - Reference: `#prompt:custom_report-to-lead`
        - Or For JetBrains: `/custom_report-to-lead`
   - Add context: `#file:docs/epic_weather_persistence_testing/plans/IMPLEMENTATION_PLAN.md` and `#file:docs/epic_weather_persistence_testing/MANIFEST.md`
   - Request: "Generate the completion report for the testing epic."

## Tips for Success

- **One agent, one task, one chat session** - Don't mix contexts
- **Double-check model selection** - Every time you switch threads, verify the model
- **Use Claude Sonnet 4/4.5 for implementation** - It's superior for code generation and detailed planning
- **Read everything** - The agents generate detailed documentation for a reason
- **Run Maven frequently** - Use `mvn clean package` after each successful task or epic
- **Follow checkstyle rules** - The project has established coding standards in checkstyle.xml
- **Trust but verify** - Agents follow patterns but can make mistakes with Java syntax and Maven configuration
- **When in doubt, escalate** - Go back to higher abstraction levels

## Java-Specific Considerations

- **Package Structure**: Ensure all new classes follow the `com.weather.app` package structure
- **Maven Integration**: Use `mvn clean compile`, `mvn test`, and `mvn checkstyle:check` regularly
- **Logging**: Use `java.util.logging.Logger` following existing patterns in the codebase
- **Exception Handling**: Follow existing patterns with `WeatherApiException` and custom exceptions
- **Configuration**: Follow existing patterns in `ConfigUtil` for configuration management
- **Resource Management**: Use try-with-resources for proper resource handling
- **Testing**: Use JUnit 5 and Mockito following existing test patterns in the codebase

This system will help you master agent-based development while building real Java applications. When you find issues, use Copilot to improve the prompts and share your enhancements with the community.