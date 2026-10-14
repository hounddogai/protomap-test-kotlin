package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.text.normalize

/** A rule a scheduled dwell adjustment must pass. Check returns its problems, or none. */
interface ScheduledDwellAdjustmentRule {
    fun check(item: ScheduledDwellAdjustment): MutableList<String>
}

data class ScheduledDwellAdjustmentRequiredLaneRule(var message: String) : ScheduledDwellAdjustmentRule {
    override fun check(item: ScheduledDwellAdjustment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellAdjustmentLaneLengthRule(var maxLength: Int) : ScheduledDwellAdjustmentRule {
    override fun check(item: ScheduledDwellAdjustment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellAdjustmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : ScheduledDwellAdjustmentRule {
    override fun check(item: ScheduledDwellAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellAdjustmentCodeLengthRule(var maxLength: Int) : ScheduledDwellAdjustmentRule {
    override fun check(item: ScheduledDwellAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellAdjustmentRequiredTitleRule(var message: String) : ScheduledDwellAdjustmentRule {
    override fun check(item: ScheduledDwellAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dwell adjustment. */
data class ScheduledDwellAdjustmentValidator(var rules: MutableList<ScheduledDwellAdjustmentRule>) {
    fun validate(item: ScheduledDwellAdjustment): MutableList<String> {
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

    fun requireValid(item: ScheduledDwellAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDwellAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDwellAdjustmentValidator(): ScheduledDwellAdjustmentValidator {
    return ScheduledDwellAdjustmentValidator(
        mutableListOf<ScheduledDwellAdjustmentRule>(
            ScheduledDwellAdjustmentRequiredLaneRule("scheduled dwell adjustment needs an entry"),
            ScheduledDwellAdjustmentLaneLengthRule(120),
            ScheduledDwellAdjustmentAllowedTitleRule(mutableListOf<String>("lenient", "pending")),
            ScheduledDwellAdjustmentCodeLengthRule(86400),
            ScheduledDwellAdjustmentRequiredTitleRule("scheduled dwell adjustment needs this field"),
        ),
    )
}
