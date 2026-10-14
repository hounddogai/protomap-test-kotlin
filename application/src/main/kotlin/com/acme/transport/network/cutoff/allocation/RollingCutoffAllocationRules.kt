package com.acme.transport.network.cutoff.allocation

/** A rule a rolling cutoff allocation must pass. Check returns its problems, or none. */
interface RollingCutoffAllocationRule {
    fun check(item: RollingCutoffAllocation): MutableList<String>
}

data class RollingCutoffAllocationStopCountRangeRule(var min: Int, var max: Int) : RollingCutoffAllocationRule {
    override fun check(item: RollingCutoffAllocation): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffAllocationPriorityRangeRule(var min: Int, var max: Int) : RollingCutoffAllocationRule {
    override fun check(item: RollingCutoffAllocation): MutableList<String> {
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

data class RollingCutoffAllocationNameLengthRule(var maxLength: Int) : RollingCutoffAllocationRule {
    override fun check(item: RollingCutoffAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffAllocationOdometerRangeRule(var min: Int, var max: Int) : RollingCutoffAllocationRule {
    override fun check(item: RollingCutoffAllocation): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling cutoff allocation. */
data class RollingCutoffAllocationValidator(var rules: MutableList<RollingCutoffAllocationRule>) {
    fun validate(item: RollingCutoffAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCutoffAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCutoffAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCutoffAllocationValidator(): RollingCutoffAllocationValidator {
    return RollingCutoffAllocationValidator(
        mutableListOf<RollingCutoffAllocationRule>(
            RollingCutoffAllocationStopCountRangeRule(80, 3600),
            RollingCutoffAllocationPriorityRangeRule(80, 86400),
            RollingCutoffAllocationNameLengthRule(255),
            RollingCutoffAllocationOdometerRangeRule(32, 64),
        ),
    )
}
