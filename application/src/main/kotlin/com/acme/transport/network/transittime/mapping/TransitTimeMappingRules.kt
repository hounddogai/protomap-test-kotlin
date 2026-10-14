package com.acme.transport.network.transittime.mapping

import com.acme.transport.kit.text.normalize

/** A rule a transit time mapping must pass. Check returns its problems, or none. */
interface TransitTimeMappingRule {
    fun check(item: TransitTimeMapping): MutableList<String>
}

data class TransitTimeMappingAllowedTitleRule(var allowed: MutableList<String>) : TransitTimeMappingRule {
    override fun check(item: TransitTimeMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMappingOwnerIdLengthRule(var maxLength: Int) : TransitTimeMappingRule {
    override fun check(item: TransitTimeMapping): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMappingTitleLengthRule(var maxLength: Int) : TransitTimeMappingRule {
    override fun check(item: TransitTimeMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMappingSummaryLengthRule(var maxLength: Int) : TransitTimeMappingRule {
    override fun check(item: TransitTimeMapping): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMappingCodeLengthRule(var maxLength: Int) : TransitTimeMappingRule {
    override fun check(item: TransitTimeMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time mapping. */
data class TransitTimeMappingValidator(var rules: MutableList<TransitTimeMappingRule>) {
    fun validate(item: TransitTimeMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeMappingValidator(): TransitTimeMappingValidator {
    return TransitTimeMappingValidator(
        mutableListOf<TransitTimeMappingRule>(
            TransitTimeMappingAllowedTitleRule(mutableListOf<String>("lenient", "fallback")),
            TransitTimeMappingOwnerIdLengthRule(80),
            TransitTimeMappingTitleLengthRule(64),
            TransitTimeMappingSummaryLengthRule(120),
            TransitTimeMappingCodeLengthRule(86400),
        ),
    )
}
