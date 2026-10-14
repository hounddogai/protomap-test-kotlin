package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.text.normalize

/** A rule a primary rest break notice must pass. Check returns its problems, or none. */
interface PrimaryRestBreakNoticeRule {
    fun check(item: PrimaryRestBreakNotice): MutableList<String>
}

data class PrimaryRestBreakNoticeRevisionRangeRule(var min: Int, var max: Int) : PrimaryRestBreakNoticeRule {
    override fun check(item: PrimaryRestBreakNotice): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class PrimaryRestBreakNoticeAllowedNameRule(
    var allowed: MutableList<String>,
) : PrimaryRestBreakNoticeRule {
    override fun check(item: PrimaryRestBreakNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRestBreakNoticeRequiredCodeRule(var message: String) : PrimaryRestBreakNoticeRule {
    override fun check(item: PrimaryRestBreakNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRestBreakNoticeRequiredNameRule(var message: String) : PrimaryRestBreakNoticeRule {
    override fun check(item: PrimaryRestBreakNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRestBreakNoticeCodeLengthRule(var maxLength: Int) : PrimaryRestBreakNoticeRule {
    override fun check(item: PrimaryRestBreakNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRestBreakNoticeCodePrefixRule(var prefix: String) : PrimaryRestBreakNoticeRule {
    override fun check(item: PrimaryRestBreakNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary rest break notice. */
data class PrimaryRestBreakNoticeValidator(var rules: MutableList<PrimaryRestBreakNoticeRule>) {
    fun validate(item: PrimaryRestBreakNotice): MutableList<String> {
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

    fun requireValid(item: PrimaryRestBreakNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRestBreakNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRestBreakNoticeValidator(): PrimaryRestBreakNoticeValidator {
    return PrimaryRestBreakNoticeValidator(
        mutableListOf<PrimaryRestBreakNoticeRule>(
            PrimaryRestBreakNoticeRevisionRangeRule(255, 1000),
            PrimaryRestBreakNoticeAllowedNameRule(mutableListOf<String>("capped", "seasonal", "regional")),
            PrimaryRestBreakNoticeRequiredCodeRule("primary rest break notice needs an entry"),
            PrimaryRestBreakNoticeRequiredNameRule("primary rest break notice needs a value"),
            PrimaryRestBreakNoticeCodeLengthRule(32),
            PrimaryRestBreakNoticeCodePrefixRule("ACM"),
        ),
    )
}
