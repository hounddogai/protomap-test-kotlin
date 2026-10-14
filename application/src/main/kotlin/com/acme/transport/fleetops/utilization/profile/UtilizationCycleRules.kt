package com.acme.transport.fleetops.utilization.profile

/** A rule a utilization cycle must pass. Check returns its problems, or none. */
interface UtilizationCycleRule {
    fun check(item: UtilizationCycle): MutableList<String>
}

data class UtilizationCycleLabelLengthRule(var maxLength: Int) : UtilizationCycleRule {
    override fun check(item: UtilizationCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationCycleRequiredExternalRefRule(var message: String) : UtilizationCycleRule {
    override fun check(item: UtilizationCycle): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationCyclePriorityRangeRule(var min: Int, var max: Int) : UtilizationCycleRule {
    override fun check(item: UtilizationCycle): MutableList<String> {
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

data class UtilizationCycleExternalRefLengthRule(var maxLength: Int) : UtilizationCycleRule {
    override fun check(item: UtilizationCycle): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization cycle. */
data class UtilizationCycleValidator(var rules: MutableList<UtilizationCycleRule>) {
    fun validate(item: UtilizationCycle): MutableList<String> {
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

    fun requireValid(item: UtilizationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationCycleValidator(): UtilizationCycleValidator {
    return UtilizationCycleValidator(
        mutableListOf<UtilizationCycleRule>(
            UtilizationCycleLabelLengthRule(64),
            UtilizationCycleRequiredExternalRefRule("utilization cycle needs a value"),
            UtilizationCyclePriorityRangeRule(120, 120),
            UtilizationCycleExternalRefLengthRule(120),
        ),
    )
}
