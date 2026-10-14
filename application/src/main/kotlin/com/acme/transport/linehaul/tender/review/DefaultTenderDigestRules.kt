package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.text.normalize

/** A rule a default tender digest must pass. Check returns its problems, or none. */
interface DefaultTenderDigestRule {
    fun check(item: DefaultTenderDigest): MutableList<String>
}

data class DefaultTenderDigestRequiredSummaryRule(var message: String) : DefaultTenderDigestRule {
    override fun check(item: DefaultTenderDigest): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTenderDigestRequiredNameRule(var message: String) : DefaultTenderDigestRule {
    override fun check(item: DefaultTenderDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTenderDigestCodeLengthRule(var maxLength: Int) : DefaultTenderDigestRule {
    override fun check(item: DefaultTenderDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTenderDigestAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : DefaultTenderDigestRule {
    override fun check(item: DefaultTenderDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTenderDigestCreatedAtRangeRule(var min: Int, var max: Int) : DefaultTenderDigestRule {
    override fun check(item: DefaultTenderDigest): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTenderDigestNameLengthRule(var maxLength: Int) : DefaultTenderDigestRule {
    override fun check(item: DefaultTenderDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tender digest. */
data class DefaultTenderDigestValidator(var rules: MutableList<DefaultTenderDigestRule>) {
    fun validate(item: DefaultTenderDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTenderDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTenderDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTenderDigestValidator(): DefaultTenderDigestValidator {
    return DefaultTenderDigestValidator(
        mutableListOf<DefaultTenderDigestRule>(
            DefaultTenderDigestRequiredSummaryRule("default tender digest needs this field"),
            DefaultTenderDigestRequiredNameRule("default tender digest needs this field"),
            DefaultTenderDigestCodeLengthRule(16),
            DefaultTenderDigestAllowedContactEmailRule(mutableListOf<String>()),
            DefaultTenderDigestCreatedAtRangeRule(16, 86400),
            DefaultTenderDigestNameLengthRule(3600),
        ),
    )
}
