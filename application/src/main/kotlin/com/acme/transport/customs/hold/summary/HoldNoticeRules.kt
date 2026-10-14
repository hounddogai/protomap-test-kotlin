package com.acme.transport.customs.hold.summary

/** A rule a hold notice must pass. Check returns its problems, or none. */
interface HoldNoticeRule {
    fun check(item: HoldNotice): MutableList<String>
}

data class HoldNoticeDriverIdLengthRule(var maxLength: Int) : HoldNoticeRule {
    override fun check(item: HoldNotice): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldNoticeCodePrefixRule(var prefix: String) : HoldNoticeRule {
    override fun check(item: HoldNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HoldNoticeCapacityRangeRule(var min: Int, var max: Int) : HoldNoticeRule {
    override fun check(item: HoldNotice): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class HoldNoticeRequiredDriverIdRule(var message: String) : HoldNoticeRule {
    override fun check(item: HoldNotice): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldNoticeTitleLengthRule(var maxLength: Int) : HoldNoticeRule {
    override fun check(item: HoldNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold notice. */
data class HoldNoticeValidator(var rules: MutableList<HoldNoticeRule>) {
    fun validate(item: HoldNotice): MutableList<String> {
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

    fun requireValid(item: HoldNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldNoticeValidator(): HoldNoticeValidator {
    return HoldNoticeValidator(
        mutableListOf<HoldNoticeRule>(
            HoldNoticeDriverIdLengthRule(255),
            HoldNoticeCodePrefixRule("ACM"),
            HoldNoticeCapacityRangeRule(32, 86400),
            HoldNoticeRequiredDriverIdRule("hold notice needs an entry"),
            HoldNoticeTitleLengthRule(64),
        ),
    )
}
