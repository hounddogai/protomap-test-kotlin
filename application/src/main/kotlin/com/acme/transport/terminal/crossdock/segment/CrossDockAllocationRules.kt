package com.acme.transport.terminal.crossdock.segment

/** A rule a cross dock allocation must pass. Check returns its problems, or none. */
interface CrossDockAllocationRule {
    fun check(item: CrossDockAllocation): MutableList<String>
}

data class CrossDockAllocationRequiredExternalRefRule(var message: String) : CrossDockAllocationRule {
    override fun check(item: CrossDockAllocation): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAllocationDockLengthRule(var maxLength: Int) : CrossDockAllocationRule {
    override fun check(item: CrossDockAllocation): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAllocationRequiredDockRule(var message: String) : CrossDockAllocationRule {
    override fun check(item: CrossDockAllocation): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAllocationOdometerRangeRule(var min: Int, var max: Int) : CrossDockAllocationRule {
    override fun check(item: CrossDockAllocation): MutableList<String> {
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

/** Runs every rule of a cross dock allocation. */
data class CrossDockAllocationValidator(var rules: MutableList<CrossDockAllocationRule>) {
    fun validate(item: CrossDockAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockAllocationValidator(): CrossDockAllocationValidator {
    return CrossDockAllocationValidator(
        mutableListOf<CrossDockAllocationRule>(
            CrossDockAllocationRequiredExternalRefRule("cross dock allocation needs a value"),
            CrossDockAllocationDockLengthRule(255),
            CrossDockAllocationRequiredDockRule("cross dock allocation needs a value"),
            CrossDockAllocationOdometerRangeRule(16, 86400),
        ),
    )
}
