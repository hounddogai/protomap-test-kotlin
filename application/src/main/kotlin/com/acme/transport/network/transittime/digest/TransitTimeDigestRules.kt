package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.normalize

/** A rule a transit time digest must pass. Check returns its problems, or none. */
interface TransitTimeDigestRule {
    fun check(item: TransitTimeDigest): MutableList<String>
}

data class TransitTimeDigestCodeLengthRule(var maxLength: Int) : TransitTimeDigestRule {
    override fun check(item: TransitTimeDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeDigestStartsAtRangeRule(var min: Int, var max: Int) : TransitTimeDigestRule {
    override fun check(item: TransitTimeDigest): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeDigestAllowedContactEmailRule(var allowed: MutableList<String>) : TransitTimeDigestRule {
    override fun check(item: TransitTimeDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeDigestLabelLengthRule(var maxLength: Int) : TransitTimeDigestRule {
    override fun check(item: TransitTimeDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeDigestCategoryLengthRule(var maxLength: Int) : TransitTimeDigestRule {
    override fun check(item: TransitTimeDigest): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time digest. */
data class TransitTimeDigestValidator(var rules: MutableList<TransitTimeDigestRule>) {
    fun validate(item: TransitTimeDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeDigestValidator(): TransitTimeDigestValidator {
    return TransitTimeDigestValidator(
        mutableListOf<TransitTimeDigestRule>(
            TransitTimeDigestCodeLengthRule(86400),
            TransitTimeDigestStartsAtRangeRule(80, 1000),
            TransitTimeDigestAllowedContactEmailRule(mutableListOf<String>("global")),
            TransitTimeDigestLabelLengthRule(16),
            TransitTimeDigestCategoryLengthRule(32),
        ),
    )
}
