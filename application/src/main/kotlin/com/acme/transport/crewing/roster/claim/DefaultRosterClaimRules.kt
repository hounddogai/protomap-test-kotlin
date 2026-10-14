package com.acme.transport.crewing.roster.claim

/** A rule a default roster claim must pass. Check returns its problems, or none. */
interface DefaultRosterClaimRule {
    fun check(item: DefaultRosterClaim): MutableList<String>
}

data class DefaultRosterClaimRetriesRangeRule(var min: Int, var max: Int) : DefaultRosterClaimRule {
    override fun check(item: DefaultRosterClaim): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterClaimRequiredCurrencyRule(var message: String) : DefaultRosterClaimRule {
    override fun check(item: DefaultRosterClaim): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterClaimDistanceKmRangeRule(var min: Double, var max: Double) : DefaultRosterClaimRule {
    override fun check(item: DefaultRosterClaim): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterClaimCreatedAtRangeRule(var min: Int, var max: Int) : DefaultRosterClaimRule {
    override fun check(item: DefaultRosterClaim): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default roster claim. */
data class DefaultRosterClaimValidatorExtra(var rules: MutableList<DefaultRosterClaimRule>) {
    fun validate(item: DefaultRosterClaim): MutableList<String> {
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

    fun requireValid(item: DefaultRosterClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRosterClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRosterClaimValidator(): DefaultRosterClaimValidatorExtra {
    return DefaultRosterClaimValidatorExtra(
        mutableListOf<DefaultRosterClaimRule>(
            DefaultRosterClaimRetriesRangeRule(16, 32),
            DefaultRosterClaimRequiredCurrencyRule("default roster claim needs this field"),
            DefaultRosterClaimDistanceKmRangeRule(1.4, 3693.2),
            DefaultRosterClaimCreatedAtRangeRule(80, 120),
        ),
    )
}
