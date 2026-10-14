package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.text.normalize

/** A rule a legacy rate card segment must pass. Check returns its problems, or none. */
interface LegacyRateCardSegmentRule {
    fun check(item: LegacyRateCardSegment): MutableList<String>
}

data class LegacyRateCardSegmentAllowedLabelRule(var allowed: MutableList<String>) : LegacyRateCardSegmentRule {
    override fun check(item: LegacyRateCardSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardSegmentRequiredCodeRule(var message: String) : LegacyRateCardSegmentRule {
    override fun check(item: LegacyRateCardSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardSegmentLabelLengthRule(var maxLength: Int) : LegacyRateCardSegmentRule {
    override fun check(item: LegacyRateCardSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardSegmentCodePrefixRule(var prefix: String) : LegacyRateCardSegmentRule {
    override fun check(item: LegacyRateCardSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardSegmentRequiredSegmentRule(var message: String) : LegacyRateCardSegmentRule {
    override fun check(item: LegacyRateCardSegment): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardSegmentRequiredLabelRule(var message: String) : LegacyRateCardSegmentRule {
    override fun check(item: LegacyRateCardSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy rate card segment. */
data class LegacyRateCardSegmentValidator(var rules: MutableList<LegacyRateCardSegmentRule>) {
    fun validate(item: LegacyRateCardSegment): MutableList<String> {
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

    fun requireValid(item: LegacyRateCardSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRateCardSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRateCardSegmentValidator(): LegacyRateCardSegmentValidator {
    return LegacyRateCardSegmentValidator(
        mutableListOf<LegacyRateCardSegmentRule>(
            LegacyRateCardSegmentAllowedLabelRule(mutableListOf<String>("primary", "manual")),
            LegacyRateCardSegmentRequiredCodeRule("legacy rate card segment needs this field"),
            LegacyRateCardSegmentLabelLengthRule(80),
            LegacyRateCardSegmentCodePrefixRule("OPS"),
            LegacyRateCardSegmentRequiredSegmentRule("legacy rate card segment needs this field"),
            LegacyRateCardSegmentRequiredLabelRule("legacy rate card segment needs an entry"),
        ),
    )
}
