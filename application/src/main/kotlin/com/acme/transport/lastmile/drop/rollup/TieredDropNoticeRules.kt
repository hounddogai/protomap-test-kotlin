package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.text.normalize

/** A rule a tiered drop notice must pass. Check returns its problems, or none. */
interface TieredDropNoticeRule {
    fun check(item: TieredDropNotice): MutableList<String>
}

data class TieredDropNoticeCodeLengthRule(var maxLength: Int) : TieredDropNoticeRule {
    override fun check(item: TieredDropNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropNoticeRequiredExternalRefRule(var message: String) : TieredDropNoticeRule {
    override fun check(item: TieredDropNotice): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDropNoticeScoreRangeRule(var min: Double, var max: Double) : TieredDropNoticeRule {
    override fun check(item: TieredDropNotice): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropNoticeAllowedCodeRule(var allowed: MutableList<String>) : TieredDropNoticeRule {
    override fun check(item: TieredDropNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropNoticeTitleLengthRule(var maxLength: Int) : TieredDropNoticeRule {
    override fun check(item: TieredDropNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropNoticeExternalRefLengthRule(var maxLength: Int) : TieredDropNoticeRule {
    override fun check(item: TieredDropNotice): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered drop notice. */
data class TieredDropNoticeValidator(var rules: MutableList<TieredDropNoticeRule>) {
    fun validate(item: TieredDropNotice): MutableList<String> {
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

    fun requireValid(item: TieredDropNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDropNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDropNoticeValidator(): TieredDropNoticeValidator {
    return TieredDropNoticeValidator(
        mutableListOf<TieredDropNoticeRule>(
            TieredDropNoticeCodeLengthRule(32),
            TieredDropNoticeRequiredExternalRefRule("tiered drop notice needs a value"),
            TieredDropNoticeScoreRangeRule(2.3, 469.9),
            TieredDropNoticeAllowedCodeRule(mutableListOf<String>("regional")),
            TieredDropNoticeTitleLengthRule(1000),
            TieredDropNoticeExternalRefLengthRule(64),
        ),
    )
}
