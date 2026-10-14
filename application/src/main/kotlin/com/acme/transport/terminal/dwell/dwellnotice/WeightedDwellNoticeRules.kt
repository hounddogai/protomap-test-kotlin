package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.text.normalize

/** A rule a weighted dwell notice must pass. Check returns its problems, or none. */
interface WeightedDwellNoticeRule {
    fun check(item: WeightedDwellNotice): MutableList<String>
}

data class WeightedDwellNoticeRequiredShiftCodeRule(var message: String) : WeightedDwellNoticeRule {
    override fun check(item: WeightedDwellNotice): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellNoticeRequiredCodeRule(var message: String) : WeightedDwellNoticeRule {
    override fun check(item: WeightedDwellNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellNoticeCodeLengthRule(var maxLength: Int) : WeightedDwellNoticeRule {
    override fun check(item: WeightedDwellNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellNoticeCodePrefixRule(var prefix: String) : WeightedDwellNoticeRule {
    override fun check(item: WeightedDwellNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellNoticeAllowedCodeRule(var allowed: MutableList<String>) : WeightedDwellNoticeRule {
    override fun check(item: WeightedDwellNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted dwell notice. */
data class WeightedDwellNoticeValidator(var rules: MutableList<WeightedDwellNoticeRule>) {
    fun validate(item: WeightedDwellNotice): MutableList<String> {
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

    fun requireValid(item: WeightedDwellNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDwellNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDwellNoticeValidator(): WeightedDwellNoticeValidator {
    return WeightedDwellNoticeValidator(
        mutableListOf<WeightedDwellNoticeRule>(
            WeightedDwellNoticeRequiredShiftCodeRule("weighted dwell notice needs a value"),
            WeightedDwellNoticeRequiredCodeRule("weighted dwell notice needs an entry"),
            WeightedDwellNoticeCodeLengthRule(3600),
            WeightedDwellNoticeCodePrefixRule("REF"),
            WeightedDwellNoticeAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
