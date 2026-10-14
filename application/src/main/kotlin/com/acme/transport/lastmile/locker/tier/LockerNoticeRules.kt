package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.text.normalize

/** A rule a locker notice must pass. Check returns its problems, or none. */
interface LockerNoticeRule {
    fun check(item: LockerNotice): MutableList<String>
}

data class LockerNoticeRequiredCodeRule(var message: String) : LockerNoticeRule {
    override fun check(item: LockerNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerNoticeCodeLengthRule(var maxLength: Int) : LockerNoticeRule {
    override fun check(item: LockerNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerNoticeRequiredDescriptionRule(var message: String) : LockerNoticeRule {
    override fun check(item: LockerNotice): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerNoticeStopCountRangeRule(var min: Int, var max: Int) : LockerNoticeRule {
    override fun check(item: LockerNotice): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerNoticeAllowedDescriptionRule(var allowed: MutableList<String>) : LockerNoticeRule {
    override fun check(item: LockerNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LockerNoticeSourceLengthRule(var maxLength: Int) : LockerNoticeRule {
    override fun check(item: LockerNotice): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker notice. */
data class LockerNoticeValidator(var rules: MutableList<LockerNoticeRule>) {
    fun validate(item: LockerNotice): MutableList<String> {
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

    fun requireValid(item: LockerNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerNoticeValidator(): LockerNoticeValidator {
    return LockerNoticeValidator(
        mutableListOf<LockerNoticeRule>(
            LockerNoticeRequiredCodeRule("locker notice needs this field"),
            LockerNoticeCodeLengthRule(86400),
            LockerNoticeRequiredDescriptionRule("locker notice needs a value"),
            LockerNoticeStopCountRangeRule(120, 86400),
            LockerNoticeAllowedDescriptionRule(mutableListOf<String>()),
            LockerNoticeSourceLengthRule(16),
        ),
    )
}
