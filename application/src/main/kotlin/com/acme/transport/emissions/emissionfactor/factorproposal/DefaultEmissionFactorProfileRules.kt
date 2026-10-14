package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.kit.text.normalize

/** A rule a default emission factor profile must pass. Check returns its problems, or none. */
interface DefaultEmissionFactorProfileRule {
    fun check(item: DefaultEmissionFactorProfile): MutableList<String>
}

data class DefaultEmissionFactorProfileRequiredCategoryRule(
    var message: String,
) : DefaultEmissionFactorProfileRule {
    override fun check(item: DefaultEmissionFactorProfile): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorProfileRequiredTitleRule(
    var message: String,
) : DefaultEmissionFactorProfileRule {
    override fun check(item: DefaultEmissionFactorProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorProfileAllowedCarrierRule(
    var allowed: MutableList<String>,
) : DefaultEmissionFactorProfileRule {
    override fun check(item: DefaultEmissionFactorProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorProfileCategoryLengthRule(
    var maxLength: Int,
) : DefaultEmissionFactorProfileRule {
    override fun check(item: DefaultEmissionFactorProfile): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorProfileCodeLengthRule(var maxLength: Int) : DefaultEmissionFactorProfileRule {
    override fun check(item: DefaultEmissionFactorProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default emission factor profile. */
data class DefaultEmissionFactorProfileValidator(var rules: MutableList<DefaultEmissionFactorProfileRule>) {
    fun validate(item: DefaultEmissionFactorProfile): MutableList<String> {
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

    fun requireValid(item: DefaultEmissionFactorProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEmissionFactorProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEmissionFactorProfileValidator(): DefaultEmissionFactorProfileValidator {
    return DefaultEmissionFactorProfileValidator(
        mutableListOf<DefaultEmissionFactorProfileRule>(
            DefaultEmissionFactorProfileRequiredCategoryRule("default emission factor profile needs a value"),
            DefaultEmissionFactorProfileRequiredTitleRule("default emission factor profile needs an entry"),
            DefaultEmissionFactorProfileAllowedCarrierRule(mutableListOf<String>()),
            DefaultEmissionFactorProfileCategoryLengthRule(86400),
            DefaultEmissionFactorProfileCodeLengthRule(255),
        ),
    )
}
