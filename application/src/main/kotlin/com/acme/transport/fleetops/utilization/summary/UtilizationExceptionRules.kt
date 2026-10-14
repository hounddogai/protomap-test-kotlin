package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.kit.text.normalize

/** A rule a utilization exception must pass. Check returns its problems, or none. */
interface UtilizationExceptionRule {
    fun check(item: UtilizationException): MutableList<String>
}

data class UtilizationExceptionRatioRangeRule(var min: Double, var max: Double) : UtilizationExceptionRule {
    override fun check(item: UtilizationException): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class UtilizationExceptionRequiredCodeRule(var message: String) : UtilizationExceptionRule {
    override fun check(item: UtilizationException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationExceptionRequiredLabelRule(var message: String) : UtilizationExceptionRule {
    override fun check(item: UtilizationException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationExceptionAllowedTitleRule(var allowed: MutableList<String>) : UtilizationExceptionRule {
    override fun check(item: UtilizationException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization exception. */
data class UtilizationExceptionValidator(var rules: MutableList<UtilizationExceptionRule>) {
    fun validate(item: UtilizationException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationException>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationExceptionValidator(): UtilizationExceptionValidator {
    return UtilizationExceptionValidator(
        mutableListOf<UtilizationExceptionRule>(
            UtilizationExceptionRatioRangeRule(4.7, 3550.7),
            UtilizationExceptionRequiredCodeRule("utilization exception needs this field"),
            UtilizationExceptionRequiredLabelRule("utilization exception needs a value"),
            UtilizationExceptionAllowedTitleRule(mutableListOf<String>("strict", "lenient")),
        ),
    )
}
