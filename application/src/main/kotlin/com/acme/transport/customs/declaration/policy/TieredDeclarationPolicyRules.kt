package com.acme.transport.customs.declaration.policy

/** A rule a tiered declaration policy must pass. Check returns its problems, or none. */
interface TieredDeclarationPolicyRule {
    fun check(item: TieredDeclarationPolicy): MutableList<String>
}

data class TieredDeclarationPolicyRequiredOwnerIdRule(var message: String) : TieredDeclarationPolicyRule {
    override fun check(item: TieredDeclarationPolicy): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationPolicyRequiredNameRule(var message: String) : TieredDeclarationPolicyRule {
    override fun check(item: TieredDeclarationPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationPolicyWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : TieredDeclarationPolicyRule {
    override fun check(item: TieredDeclarationPolicy): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationPolicyAmountRangeRule(
    var min: Double,
    var max: Double,
) : TieredDeclarationPolicyRule {
    override fun check(item: TieredDeclarationPolicy): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class TieredDeclarationPolicyScoreRangeRule(
    var min: Double,
    var max: Double,
) : TieredDeclarationPolicyRule {
    override fun check(item: TieredDeclarationPolicy): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class TieredDeclarationPolicyDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : TieredDeclarationPolicyRule {
    override fun check(item: TieredDeclarationPolicy): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered declaration policy. */
data class TieredDeclarationPolicyValidator(var rules: MutableList<TieredDeclarationPolicyRule>) {
    fun validate(item: TieredDeclarationPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDeclarationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDeclarationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDeclarationPolicyValidator(): TieredDeclarationPolicyValidator {
    return TieredDeclarationPolicyValidator(
        mutableListOf<TieredDeclarationPolicyRule>(
            TieredDeclarationPolicyRequiredOwnerIdRule("tiered declaration policy needs this field"),
            TieredDeclarationPolicyRequiredNameRule("tiered declaration policy needs a value"),
            TieredDeclarationPolicyWindowDaysRangeRule(120, 3600),
            TieredDeclarationPolicyAmountRangeRule(4.0, 4544.9),
            TieredDeclarationPolicyScoreRangeRule(1.6, 397.0),
            TieredDeclarationPolicyDistanceKmRangeRule(6.3, 2494.0),
        ),
    )
}
