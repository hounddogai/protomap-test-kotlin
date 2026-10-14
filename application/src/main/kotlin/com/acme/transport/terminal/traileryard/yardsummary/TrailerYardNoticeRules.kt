package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard notice must pass. Check returns its problems, or none. */
interface TrailerYardNoticeRule {
    fun check(item: TrailerYardNotice): MutableList<String>
}

data class TrailerYardNoticeAllowedCodeRule(var allowed: MutableList<String>) : TrailerYardNoticeRule {
    override fun check(item: TrailerYardNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardNoticeCodePrefixRule(var prefix: String) : TrailerYardNoticeRule {
    override fun check(item: TrailerYardNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardNoticeExpiresAtRangeRule(var min: Int, var max: Int) : TrailerYardNoticeRule {
    override fun check(item: TrailerYardNotice): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class TrailerYardNoticeTitleLengthRule(var maxLength: Int) : TrailerYardNoticeRule {
    override fun check(item: TrailerYardNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardNoticeCodeLengthRule(var maxLength: Int) : TrailerYardNoticeRule {
    override fun check(item: TrailerYardNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard notice. */
data class TrailerYardNoticeValidator(var rules: MutableList<TrailerYardNoticeRule>) {
    fun validate(item: TrailerYardNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardNoticeValidator(): TrailerYardNoticeValidator {
    return TrailerYardNoticeValidator(
        mutableListOf<TrailerYardNoticeRule>(
            TrailerYardNoticeAllowedCodeRule(mutableListOf<String>("tiered", "strict", "scheduled")),
            TrailerYardNoticeCodePrefixRule("OPS"),
            TrailerYardNoticeExpiresAtRangeRule(64, 255),
            TrailerYardNoticeTitleLengthRule(1000),
            TrailerYardNoticeCodeLengthRule(1000),
        ),
    )
}
