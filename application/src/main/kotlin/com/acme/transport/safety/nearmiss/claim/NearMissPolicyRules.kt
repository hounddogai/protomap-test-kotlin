package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.text.normalize

/** A rule a near miss policy must pass. Check returns its problems, or none. */
interface NearMissPolicyRule {
    fun check(item: NearMissPolicy): MutableList<String>
}

data class NearMissPolicyAllowedUnitRule(var allowed: MutableList<String>) : NearMissPolicyRule {
    override fun check(item: NearMissPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is expired")
        }
        return mutableListOf<String>()
    }
}

data class NearMissPolicyRequiredUnitRule(var message: String) : NearMissPolicyRule {
    override fun check(item: NearMissPolicy): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissPolicyCodePrefixRule(var prefix: String) : NearMissPolicyRule {
    override fun check(item: NearMissPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissPolicyLimitRangeRule(var min: Int, var max: Int) : NearMissPolicyRule {
    override fun check(item: NearMissPolicy): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a near miss policy. */
data class NearMissPolicyValidator(var rules: MutableList<NearMissPolicyRule>) {
    fun validate(item: NearMissPolicy): MutableList<String> {
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

    fun requireValid(item: NearMissPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissPolicyValidator(): NearMissPolicyValidator {
    return NearMissPolicyValidator(
        mutableListOf<NearMissPolicyRule>(
            NearMissPolicyAllowedUnitRule(mutableListOf<String>("pending")),
            NearMissPolicyRequiredUnitRule("near miss policy needs this field"),
            NearMissPolicyCodePrefixRule("REF"),
            NearMissPolicyLimitRangeRule(32, 3600),
        ),
    )
}
