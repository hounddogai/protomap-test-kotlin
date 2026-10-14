package com.acme.transport.terminal.dwell.dwellcycle

/** A rule a scheduled dwell allocation must pass. Check returns its problems, or none. */
interface ScheduledDwellAllocationRule {
    fun check(item: ScheduledDwellAllocation): MutableList<String>
}

data class ScheduledDwellAllocationRequiredCategoryRule(var message: String) : ScheduledDwellAllocationRule {
    override fun check(item: ScheduledDwellAllocation): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellAllocationContactEmailLengthRule(var maxLength: Int) : ScheduledDwellAllocationRule {
    override fun check(item: ScheduledDwellAllocation): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellAllocationEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledDwellAllocationRule {
    override fun check(item: ScheduledDwellAllocation): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dwell allocation. */
data class ScheduledDwellAllocationValidator(var rules: MutableList<ScheduledDwellAllocationRule>) {
    fun validate(item: ScheduledDwellAllocation): MutableList<String> {
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

    fun requireValid(item: ScheduledDwellAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDwellAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDwellAllocationValidator(): ScheduledDwellAllocationValidator {
    return ScheduledDwellAllocationValidator(
        mutableListOf<ScheduledDwellAllocationRule>(
            ScheduledDwellAllocationRequiredCategoryRule("scheduled dwell allocation needs an entry"),
            ScheduledDwellAllocationContactEmailLengthRule(64),
            ScheduledDwellAllocationEtaMinutesRangeRule(64, 80),
        ),
    )
}
