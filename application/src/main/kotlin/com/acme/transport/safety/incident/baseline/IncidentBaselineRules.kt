package com.acme.transport.safety.incident.baseline

/** A rule a incident baseline must pass. Check returns its problems, or none. */
interface IncidentBaselineRule {
    fun check(item: IncidentBaseline): MutableList<String>
}

data class IncidentBaselineCodeLengthRule(var maxLength: Int) : IncidentBaselineRule {
    override fun check(item: IncidentBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentBaselineFuelLitersRangeRule(var min: Double, var max: Double) : IncidentBaselineRule {
    override fun check(item: IncidentBaseline): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class IncidentBaselinePriorityRangeRule(var min: Int, var max: Int) : IncidentBaselineRule {
    override fun check(item: IncidentBaseline): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a incident baseline. */
data class IncidentBaselineValidator(var rules: MutableList<IncidentBaselineRule>) {
    fun validate(item: IncidentBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentBaselineValidator(): IncidentBaselineValidator {
    return IncidentBaselineValidator(
        mutableListOf<IncidentBaselineRule>(
            IncidentBaselineCodeLengthRule(255),
            IncidentBaselineFuelLitersRangeRule(0.8, 4120.2),
            IncidentBaselinePriorityRangeRule(80, 86400),
        ),
    )
}
