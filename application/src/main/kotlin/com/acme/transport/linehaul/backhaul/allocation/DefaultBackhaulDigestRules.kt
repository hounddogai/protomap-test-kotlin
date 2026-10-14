package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.text.normalize

/** A rule a default backhaul digest must pass. Check returns its problems, or none. */
interface DefaultBackhaulDigestRule {
    fun check(item: DefaultBackhaulDigest): MutableList<String>
}

data class DefaultBackhaulDigestCodePrefixRule(var prefix: String) : DefaultBackhaulDigestRule {
    override fun check(item: DefaultBackhaulDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultBackhaulDigestRequiredTimezoneRule(var message: String) : DefaultBackhaulDigestRule {
    override fun check(item: DefaultBackhaulDigest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultBackhaulDigestTimezoneLengthRule(var maxLength: Int) : DefaultBackhaulDigestRule {
    override fun check(item: DefaultBackhaulDigest): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultBackhaulDigestNameLengthRule(var maxLength: Int) : DefaultBackhaulDigestRule {
    override fun check(item: DefaultBackhaulDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultBackhaulDigestAllowedCodeRule(var allowed: MutableList<String>) : DefaultBackhaulDigestRule {
    override fun check(item: DefaultBackhaulDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default backhaul digest. */
data class DefaultBackhaulDigestValidator(var rules: MutableList<DefaultBackhaulDigestRule>) {
    fun validate(item: DefaultBackhaulDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultBackhaulDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultBackhaulDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultBackhaulDigestValidator(): DefaultBackhaulDigestValidator {
    return DefaultBackhaulDigestValidator(
        mutableListOf<DefaultBackhaulDigestRule>(
            DefaultBackhaulDigestCodePrefixRule("X"),
            DefaultBackhaulDigestRequiredTimezoneRule("default backhaul digest needs an entry"),
            DefaultBackhaulDigestTimezoneLengthRule(80),
            DefaultBackhaulDigestNameLengthRule(255),
            DefaultBackhaulDigestAllowedCodeRule(mutableListOf<String>("seasonal", "weighted", "default")),
        ),
    )
}
