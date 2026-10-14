package com.acme.transport.emissions.routeoffset.baseline

/** A rule a manual route offset baseline must pass. Check returns its problems, or none. */
interface ManualRouteOffsetBaselineRule {
    fun check(item: ManualRouteOffsetBaseline): MutableList<String>
}

data class ManualRouteOffsetBaselineRequiredCodeRule(var message: String) : ManualRouteOffsetBaselineRule {
    override fun check(item: ManualRouteOffsetBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRouteOffsetBaselineNameLengthRule(var maxLength: Int) : ManualRouteOffsetBaselineRule {
    override fun check(item: ManualRouteOffsetBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRouteOffsetBaselineScoreRangeRule(
    var min: Double,
    var max: Double,
) : ManualRouteOffsetBaselineRule {
    override fun check(item: ManualRouteOffsetBaseline): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual route offset baseline. */
data class ManualRouteOffsetBaselineValidator(var rules: MutableList<ManualRouteOffsetBaselineRule>) {
    fun validate(item: ManualRouteOffsetBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: ManualRouteOffsetBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRouteOffsetBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRouteOffsetBaselineValidator(): ManualRouteOffsetBaselineValidator {
    return ManualRouteOffsetBaselineValidator(
        mutableListOf<ManualRouteOffsetBaselineRule>(
            ManualRouteOffsetBaselineRequiredCodeRule("manual route offset baseline needs this field"),
            ManualRouteOffsetBaselineNameLengthRule(32),
            ManualRouteOffsetBaselineScoreRangeRule(8.7, 1487.6),
        ),
    )
}
