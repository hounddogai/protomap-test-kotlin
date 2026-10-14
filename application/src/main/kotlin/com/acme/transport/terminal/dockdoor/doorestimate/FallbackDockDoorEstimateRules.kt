package com.acme.transport.terminal.dockdoor.doorestimate

/** A rule a fallback dock door estimate must pass. Check returns its problems, or none. */
interface FallbackDockDoorEstimateRule {
    fun check(item: FallbackDockDoorEstimate): MutableList<String>
}

data class FallbackDockDoorEstimateMinimumRangeRule(
    var min: Double,
    var max: Double,
) : FallbackDockDoorEstimateRule {
    override fun check(item: FallbackDockDoorEstimate): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorEstimateTitleLengthRule(var maxLength: Int) : FallbackDockDoorEstimateRule {
    override fun check(item: FallbackDockDoorEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorEstimateEndsAtRangeRule(var min: Int, var max: Int) : FallbackDockDoorEstimateRule {
    override fun check(item: FallbackDockDoorEstimate): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dock door estimate. */
data class FallbackDockDoorEstimateValidator(var rules: MutableList<FallbackDockDoorEstimateRule>) {
    fun validate(item: FallbackDockDoorEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDockDoorEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDockDoorEstimate>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultFallbackDockDoorEstimateValidator(): FallbackDockDoorEstimateValidator {
    return FallbackDockDoorEstimateValidator(
        mutableListOf<FallbackDockDoorEstimateRule>(
            FallbackDockDoorEstimateMinimumRangeRule(0.2, 1107.5),
            FallbackDockDoorEstimateTitleLengthRule(16),
            FallbackDockDoorEstimateEndsAtRangeRule(16, 3600),
        ),
    )
}
