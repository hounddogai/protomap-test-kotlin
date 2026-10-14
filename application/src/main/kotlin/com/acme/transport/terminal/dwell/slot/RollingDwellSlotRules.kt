package com.acme.transport.terminal.dwell.slot

/** A rule a rolling dwell slot must pass. Check returns its problems, or none. */
interface RollingDwellSlotRule {
    fun check(item: RollingDwellSlot): MutableList<String>
}

data class RollingDwellSlotRequiredSummaryRule(var message: String) : RollingDwellSlotRule {
    override fun check(item: RollingDwellSlot): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellSlotRequiredTitleRule(var message: String) : RollingDwellSlotRule {
    override fun check(item: RollingDwellSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellSlotGraceDaysRangeRule(var min: Int, var max: Int) : RollingDwellSlotRule {
    override fun check(item: RollingDwellSlot): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dwell slot. */
data class RollingDwellSlotValidator(var rules: MutableList<RollingDwellSlotRule>) {
    fun validate(item: RollingDwellSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDwellSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDwellSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDwellSlotValidator(): RollingDwellSlotValidator {
    return RollingDwellSlotValidator(
        mutableListOf<RollingDwellSlotRule>(
            RollingDwellSlotRequiredSummaryRule("rolling dwell slot needs a value"),
            RollingDwellSlotRequiredTitleRule("rolling dwell slot needs a value"),
            RollingDwellSlotGraceDaysRangeRule(64, 80),
        ),
    )
}
