package com.acme.transport.terminal.dwell.cycle

/** A rule a dwell group must pass. Check returns its problems, or none. */
interface DwellGroupRule {
    fun check(item: DwellGroup): MutableList<String>
}

data class DwellGroupRequiredCodeRule(var message: String) : DwellGroupRule {
    override fun check(item: DwellGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellGroupStopCountRangeRule(var min: Int, var max: Int) : DwellGroupRule {
    override fun check(item: DwellGroup): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class DwellGroupBatchSizeRangeRule(var min: Int, var max: Int) : DwellGroupRule {
    override fun check(item: DwellGroup): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell group. */
data class DwellGroupValidator(var rules: MutableList<DwellGroupRule>) {
    fun validate(item: DwellGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellGroupValidator(): DwellGroupValidator {
    return DwellGroupValidator(
        mutableListOf<DwellGroupRule>(
            DwellGroupRequiredCodeRule("dwell group needs this field"),
            DwellGroupStopCountRangeRule(1000, 86400),
            DwellGroupBatchSizeRangeRule(16, 1000),
        ),
    )
}
