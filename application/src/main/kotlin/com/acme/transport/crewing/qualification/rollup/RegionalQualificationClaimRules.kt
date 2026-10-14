package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.text.normalize

/** A rule a regional qualification claim must pass. Check returns its problems, or none. */
interface RegionalQualificationClaimRule {
    fun check(item: RegionalQualificationClaim): MutableList<String>
}

data class RegionalQualificationClaimRequiredCodeRule(var message: String) : RegionalQualificationClaimRule {
    override fun check(item: RegionalQualificationClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationClaimNameLengthRule(var maxLength: Int) : RegionalQualificationClaimRule {
    override fun check(item: RegionalQualificationClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationClaimAllowedNameRule(
    var allowed: MutableList<String>,
) : RegionalQualificationClaimRule {
    override fun check(item: RegionalQualificationClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional qualification claim. */
data class RegionalQualificationClaimValidator(var rules: MutableList<RegionalQualificationClaimRule>) {
    fun validate(item: RegionalQualificationClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalQualificationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalQualificationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalQualificationClaimValidator(): RegionalQualificationClaimValidator {
    return RegionalQualificationClaimValidator(
        mutableListOf<RegionalQualificationClaimRule>(
            RegionalQualificationClaimRequiredCodeRule("regional qualification claim needs this field"),
            RegionalQualificationClaimNameLengthRule(255),
            RegionalQualificationClaimAllowedNameRule(mutableListOf<String>("default", "default", "lenient")),
        ),
    )
}
