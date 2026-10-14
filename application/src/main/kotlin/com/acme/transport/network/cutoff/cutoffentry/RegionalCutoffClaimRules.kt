package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.text.normalize

/** A rule a regional cutoff claim must pass. Check returns its problems, or none. */
interface RegionalCutoffClaimRule {
    fun check(item: RegionalCutoffClaim): MutableList<String>
}

data class RegionalCutoffClaimVersionRangeRule(var min: Int, var max: Int) : RegionalCutoffClaimRule {
    override fun check(item: RegionalCutoffClaim): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class RegionalCutoffClaimTitleLengthRule(var maxLength: Int) : RegionalCutoffClaimRule {
    override fun check(item: RegionalCutoffClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffClaimAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : RegionalCutoffClaimRule {
    override fun check(item: RegionalCutoffClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffClaimExternalRefLengthRule(var maxLength: Int) : RegionalCutoffClaimRule {
    override fun check(item: RegionalCutoffClaim): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional cutoff claim. */
data class RegionalCutoffClaimValidator(var rules: MutableList<RegionalCutoffClaimRule>) {
    fun validate(item: RegionalCutoffClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCutoffClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCutoffClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCutoffClaimValidator(): RegionalCutoffClaimValidator {
    return RegionalCutoffClaimValidator(
        mutableListOf<RegionalCutoffClaimRule>(
            RegionalCutoffClaimVersionRangeRule(64, 255),
            RegionalCutoffClaimTitleLengthRule(1000),
            RegionalCutoffClaimAllowedExternalRefRule(mutableListOf<String>()),
            RegionalCutoffClaimExternalRefLengthRule(64),
        ),
    )
}
