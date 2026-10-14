package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.kit.text.normalize

/** A rule a regional rate card notice must pass. Check returns its problems, or none. */
interface RegionalRateCardNoticeRule {
    fun check(item: RegionalRateCardNotice): MutableList<String>
}

data class RegionalRateCardNoticeFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : RegionalRateCardNoticeRule {
    override fun check(item: RegionalRateCardNotice): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardNoticeRequiredNameRule(var message: String) : RegionalRateCardNoticeRule {
    override fun check(item: RegionalRateCardNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardNoticeAllowedNotesRule(
    var allowed: MutableList<String>,
) : RegionalRateCardNoticeRule {
    override fun check(item: RegionalRateCardNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardNoticeLabelLengthRule(var maxLength: Int) : RegionalRateCardNoticeRule {
    override fun check(item: RegionalRateCardNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardNoticeCodeLengthRule(var maxLength: Int) : RegionalRateCardNoticeRule {
    override fun check(item: RegionalRateCardNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardNoticeNameLengthRule(var maxLength: Int) : RegionalRateCardNoticeRule {
    override fun check(item: RegionalRateCardNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional rate card notice. */
data class RegionalRateCardNoticeValidator(var rules: MutableList<RegionalRateCardNoticeRule>) {
    fun validate(item: RegionalRateCardNotice): MutableList<String> {
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

    fun requireValid(item: RegionalRateCardNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRateCardNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRateCardNoticeValidator(): RegionalRateCardNoticeValidator {
    return RegionalRateCardNoticeValidator(
        mutableListOf<RegionalRateCardNoticeRule>(
            RegionalRateCardNoticeFloorValueRangeRule(3.5, 3405.4),
            RegionalRateCardNoticeRequiredNameRule("regional rate card notice needs an entry"),
            RegionalRateCardNoticeAllowedNotesRule(mutableListOf<String>("batched", "lenient")),
            RegionalRateCardNoticeLabelLengthRule(64),
            RegionalRateCardNoticeCodeLengthRule(3600),
            RegionalRateCardNoticeNameLengthRule(86400),
        ),
    )
}
