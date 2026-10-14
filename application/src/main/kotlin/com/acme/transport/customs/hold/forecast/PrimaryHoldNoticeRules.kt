package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.text.normalize

/** A rule a primary hold notice must pass. Check returns its problems, or none. */
interface PrimaryHoldNoticeRule {
    fun check(item: PrimaryHoldNotice): MutableList<String>
}

data class PrimaryHoldNoticeAllowedSourceRule(var allowed: MutableList<String>) : PrimaryHoldNoticeRule {
    override fun check(item: PrimaryHoldNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldNoticeRequiredNameRule(var message: String) : PrimaryHoldNoticeRule {
    override fun check(item: PrimaryHoldNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldNoticeSourceLengthRule(var maxLength: Int) : PrimaryHoldNoticeRule {
    override fun check(item: PrimaryHoldNotice): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldNoticeRequiredCurrencyRule(var message: String) : PrimaryHoldNoticeRule {
    override fun check(item: PrimaryHoldNotice): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldNoticeRequiredCodeRule(var message: String) : PrimaryHoldNoticeRule {
    override fun check(item: PrimaryHoldNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary hold notice. */
data class PrimaryHoldNoticeValidator(var rules: MutableList<PrimaryHoldNoticeRule>) {
    fun validate(item: PrimaryHoldNotice): MutableList<String> {
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

    fun requireValid(item: PrimaryHoldNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryHoldNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryHoldNoticeValidator(): PrimaryHoldNoticeValidator {
    return PrimaryHoldNoticeValidator(
        mutableListOf<PrimaryHoldNoticeRule>(
            PrimaryHoldNoticeAllowedSourceRule(mutableListOf<String>()),
            PrimaryHoldNoticeRequiredNameRule("primary hold notice needs a value"),
            PrimaryHoldNoticeSourceLengthRule(80),
            PrimaryHoldNoticeRequiredCurrencyRule("primary hold notice needs a value"),
            PrimaryHoldNoticeRequiredCodeRule("primary hold notice needs this field"),
        ),
    )
}
