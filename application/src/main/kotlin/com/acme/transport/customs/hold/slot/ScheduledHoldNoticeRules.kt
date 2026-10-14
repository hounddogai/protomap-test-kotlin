package com.acme.transport.customs.hold.slot

/** A rule a scheduled hold notice must pass. Check returns its problems, or none. */
interface ScheduledHoldNoticeRule {
    fun check(item: ScheduledHoldNotice): MutableList<String>
}

data class ScheduledHoldNoticeCodePrefixRule(var prefix: String) : ScheduledHoldNoticeRule {
    override fun check(item: ScheduledHoldNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldNoticeChannelLengthRule(var maxLength: Int) : ScheduledHoldNoticeRule {
    override fun check(item: ScheduledHoldNotice): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldNoticeCreatedAtRangeRule(var min: Int, var max: Int) : ScheduledHoldNoticeRule {
    override fun check(item: ScheduledHoldNotice): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldNoticeOdometerRangeRule(var min: Int, var max: Int) : ScheduledHoldNoticeRule {
    override fun check(item: ScheduledHoldNotice): MutableList<String> {
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

data class ScheduledHoldNoticeMarginRangeRule(var min: Double, var max: Double) : ScheduledHoldNoticeRule {
    override fun check(item: ScheduledHoldNotice): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class ScheduledHoldNoticeNameLengthRule(var maxLength: Int) : ScheduledHoldNoticeRule {
    override fun check(item: ScheduledHoldNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hold notice. */
data class ScheduledHoldNoticeValidator(var rules: MutableList<ScheduledHoldNoticeRule>) {
    fun validate(item: ScheduledHoldNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledHoldNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHoldNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHoldNoticeValidator(): ScheduledHoldNoticeValidator {
    return ScheduledHoldNoticeValidator(
        mutableListOf<ScheduledHoldNoticeRule>(
            ScheduledHoldNoticeCodePrefixRule("Q"),
            ScheduledHoldNoticeChannelLengthRule(120),
            ScheduledHoldNoticeCreatedAtRangeRule(80, 255),
            ScheduledHoldNoticeOdometerRangeRule(16, 1000),
            ScheduledHoldNoticeMarginRangeRule(1.1, 3410.6),
            ScheduledHoldNoticeNameLengthRule(255),
        ),
    )
}
