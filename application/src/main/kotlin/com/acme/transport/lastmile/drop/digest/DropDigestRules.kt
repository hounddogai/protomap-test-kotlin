package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.text.normalize

/** A rule a drop digest must pass. Check returns its problems, or none. */
interface DropDigestRule {
    fun check(item: DropDigest): MutableList<String>
}

data class DropDigestLabelLengthRule(var maxLength: Int) : DropDigestRule {
    override fun check(item: DropDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropDigestRetriesRangeRule(var min: Int, var max: Int) : DropDigestRule {
    override fun check(item: DropDigest): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class DropDigestRequiredCodeRule(var message: String) : DropDigestRule {
    override fun check(item: DropDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropDigestCodePrefixRule(var prefix: String) : DropDigestRule {
    override fun check(item: DropDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropDigestRequiredLabelRule(var message: String) : DropDigestRule {
    override fun check(item: DropDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropDigestAllowedLabelRule(var allowed: MutableList<String>) : DropDigestRule {
    override fun check(item: DropDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop digest. */
data class DropDigestValidator(var rules: MutableList<DropDigestRule>) {
    fun validate(item: DropDigest): MutableList<String> {
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

    fun requireValid(item: DropDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDropDigestValidator(): DropDigestValidator {
    return DropDigestValidator(
        mutableListOf<DropDigestRule>(
            DropDigestLabelLengthRule(32),
            DropDigestRetriesRangeRule(120, 255),
            DropDigestRequiredCodeRule("drop digest needs an entry"),
            DropDigestCodePrefixRule("Q"),
            DropDigestRequiredLabelRule("drop digest needs this field"),
            DropDigestAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
