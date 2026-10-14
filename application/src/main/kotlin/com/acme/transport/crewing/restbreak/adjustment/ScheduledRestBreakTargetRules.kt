package com.acme.transport.crewing.restbreak.adjustment

/** A rule a scheduled rest break target must pass. Check returns its problems, or none. */
interface ScheduledRestBreakTargetRule {
    fun check(item: ScheduledRestBreakTarget): MutableList<String>
}

data class ScheduledRestBreakTargetLabelLengthRule(var maxLength: Int) : ScheduledRestBreakTargetRule {
    override fun check(item: ScheduledRestBreakTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakTargetCodePrefixRule(var prefix: String) : ScheduledRestBreakTargetRule {
    override fun check(item: ScheduledRestBreakTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakTargetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledRestBreakTargetRule {
    override fun check(item: ScheduledRestBreakTarget): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class ScheduledRestBreakTargetArchivedGuardRule(var limit: Double) : ScheduledRestBreakTargetRule {
    override fun check(item: ScheduledRestBreakTarget): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakTargetRequiredLabelRule(var message: String) : ScheduledRestBreakTargetRule {
    override fun check(item: ScheduledRestBreakTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakTargetRequiredCodeRule(var message: String) : ScheduledRestBreakTargetRule {
    override fun check(item: ScheduledRestBreakTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled rest break target. */
data class ScheduledRestBreakTargetValidator(var rules: MutableList<ScheduledRestBreakTargetRule>) {
    fun validate(item: ScheduledRestBreakTarget): MutableList<String> {
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

    fun requireValid(item: ScheduledRestBreakTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRestBreakTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRestBreakTargetValidator(): ScheduledRestBreakTargetValidator {
    return ScheduledRestBreakTargetValidator(
        mutableListOf<ScheduledRestBreakTargetRule>(
            ScheduledRestBreakTargetLabelLengthRule(3600),
            ScheduledRestBreakTargetCodePrefixRule("Q"),
            ScheduledRestBreakTargetThresholdRangeRule(7.6, 3555.7),
            ScheduledRestBreakTargetArchivedGuardRule(6.9),
            ScheduledRestBreakTargetRequiredLabelRule("scheduled rest break target needs a value"),
            ScheduledRestBreakTargetRequiredCodeRule("scheduled rest break target needs a value"),
        ),
    )
}
