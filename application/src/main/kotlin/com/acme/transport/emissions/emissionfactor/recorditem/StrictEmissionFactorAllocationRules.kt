package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a strict emission factor allocation must pass. Check returns its problems, or none. */
interface StrictEmissionFactorAllocationRule {
    fun check(item: StrictEmissionFactorAllocation): MutableList<String>
}

data class StrictEmissionFactorAllocationRequiredTimezoneRule(
    var message: String,
) : StrictEmissionFactorAllocationRule {
    override fun check(item: StrictEmissionFactorAllocation): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorAllocationRequiredTitleRule(
    var message: String,
) : StrictEmissionFactorAllocationRule {
    override fun check(item: StrictEmissionFactorAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorAllocationAllowedNameRule(
    var allowed: MutableList<String>,
) : StrictEmissionFactorAllocationRule {
    override fun check(item: StrictEmissionFactorAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorAllocationCodePrefixRule(
    var prefix: String,
) : StrictEmissionFactorAllocationRule {
    override fun check(item: StrictEmissionFactorAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorAllocationRequiredCodeRule(
    var message: String,
) : StrictEmissionFactorAllocationRule {
    override fun check(item: StrictEmissionFactorAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict emission factor allocation. */
data class StrictEmissionFactorAllocationValidator(var rules: MutableList<StrictEmissionFactorAllocationRule>) {
    fun validate(item: StrictEmissionFactorAllocation): MutableList<String> {
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

    fun requireValid(item: StrictEmissionFactorAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEmissionFactorAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEmissionFactorAllocationValidator(): StrictEmissionFactorAllocationValidator {
    return StrictEmissionFactorAllocationValidator(
        mutableListOf<StrictEmissionFactorAllocationRule>(
            StrictEmissionFactorAllocationRequiredTimezoneRule(
                "strict emission factor allocation needs this field",
            ),
            StrictEmissionFactorAllocationRequiredTitleRule("strict emission factor allocation needs an entry"),
            StrictEmissionFactorAllocationAllowedNameRule(mutableListOf<String>("legacy", "strict", "primary")),
            StrictEmissionFactorAllocationCodePrefixRule("REF"),
            StrictEmissionFactorAllocationRequiredCodeRule("strict emission factor allocation needs a value"),
        ),
    )
}
