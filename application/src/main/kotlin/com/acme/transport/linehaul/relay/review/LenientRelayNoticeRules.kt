package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.text.normalize

/** A rule a lenient relay notice must pass. Check returns its problems, or none. */
interface LenientRelayNoticeRule {
    fun check(item: LenientRelayNotice): MutableList<String>
}

data class LenientRelayNoticeLabelLengthRule(var maxLength: Int) : LenientRelayNoticeRule {
    override fun check(item: LenientRelayNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayNoticeRateRangeRule(var min: Double, var max: Double) : LenientRelayNoticeRule {
    override fun check(item: LenientRelayNotice): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class LenientRelayNoticeAllowedLabelRule(var allowed: MutableList<String>) : LenientRelayNoticeRule {
    override fun check(item: LenientRelayNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient relay notice. */
data class LenientRelayNoticeValidator(var rules: MutableList<LenientRelayNoticeRule>) {
    fun validate(item: LenientRelayNotice): MutableList<String> {
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

    fun requireValid(item: LenientRelayNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRelayNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRelayNoticeValidator(): LenientRelayNoticeValidator {
    return LenientRelayNoticeValidator(
        mutableListOf<LenientRelayNoticeRule>(
            LenientRelayNoticeLabelLengthRule(16),
            LenientRelayNoticeRateRangeRule(7.7, 4959.8),
            LenientRelayNoticeAllowedLabelRule(mutableListOf<String>("manual")),
        ),
    )
}
