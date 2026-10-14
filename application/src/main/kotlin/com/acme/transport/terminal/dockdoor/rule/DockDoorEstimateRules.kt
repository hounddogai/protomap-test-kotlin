package com.acme.transport.terminal.dockdoor.rule

/** A rule a dock door estimate must pass. Check returns its problems, or none. */
interface DockDoorEstimateRule {
    fun check(item: DockDoorEstimate): MutableList<String>
}

data class DockDoorEstimateRequiredTitleRule(var message: String) : DockDoorEstimateRule {
    override fun check(item: DockDoorEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorEstimateMarginRangeRule(var min: Double, var max: Double) : DockDoorEstimateRule {
    override fun check(item: DockDoorEstimate): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorEstimateThresholdRangeRule(var min: Double, var max: Double) : DockDoorEstimateRule {
    override fun check(item: DockDoorEstimate): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door estimate. */
data class DockDoorEstimateValidator(var rules: MutableList<DockDoorEstimateRule>) {
    fun validate(item: DockDoorEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorEstimateValidator(): DockDoorEstimateValidator {
    return DockDoorEstimateValidator(
        mutableListOf<DockDoorEstimateRule>(
            DockDoorEstimateRequiredTitleRule("dock door estimate needs a value"),
            DockDoorEstimateMarginRangeRule(4.8, 3702.4),
            DockDoorEstimateThresholdRangeRule(9.1, 2830.7),
        ),
    )
}
