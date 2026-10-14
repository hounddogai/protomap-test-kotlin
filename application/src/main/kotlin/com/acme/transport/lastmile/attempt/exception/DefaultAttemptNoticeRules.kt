package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.kit.text.normalize

/** A rule a default attempt notice must pass. Check returns its problems, or none. */
interface DefaultAttemptNoticeRule {
    fun check(item: DefaultAttemptNotice): MutableList<String>
}

data class DefaultAttemptNoticeRequiredCarrierRule(var message: String) : DefaultAttemptNoticeRule {
    override fun check(item: DefaultAttemptNotice): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptNoticeCodePrefixRule(var prefix: String) : DefaultAttemptNoticeRule {
    override fun check(item: DefaultAttemptNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptNoticeAllowedCodeRule(var allowed: MutableList<String>) : DefaultAttemptNoticeRule {
    override fun check(item: DefaultAttemptNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptNoticeUnitLengthRule(var maxLength: Int) : DefaultAttemptNoticeRule {
    override fun check(item: DefaultAttemptNotice): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptNoticeRequiredUnitRule(var message: String) : DefaultAttemptNoticeRule {
    override fun check(item: DefaultAttemptNotice): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default attempt notice. */
data class DefaultAttemptNoticeValidator(var rules: MutableList<DefaultAttemptNoticeRule>) {
    fun validate(item: DefaultAttemptNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAttemptNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAttemptNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAttemptNoticeValidator(): DefaultAttemptNoticeValidator {
    return DefaultAttemptNoticeValidator(
        mutableListOf<DefaultAttemptNoticeRule>(
            DefaultAttemptNoticeRequiredCarrierRule("default attempt notice needs a value"),
            DefaultAttemptNoticeCodePrefixRule("OPS"),
            DefaultAttemptNoticeAllowedCodeRule(mutableListOf<String>("seasonal", "fallback")),
            DefaultAttemptNoticeUnitLengthRule(16),
            DefaultAttemptNoticeRequiredUnitRule("default attempt notice needs this field"),
        ),
    )
}
