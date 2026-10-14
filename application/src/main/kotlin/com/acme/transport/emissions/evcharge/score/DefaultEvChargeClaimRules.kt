package com.acme.transport.emissions.evcharge.score

/** A rule a default ev charge claim must pass. Check returns its problems, or none. */
interface DefaultEvChargeClaimRule {
    fun check(item: DefaultEvChargeClaim): MutableList<String>
}

data class DefaultEvChargeClaimFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : DefaultEvChargeClaimRule {
    override fun check(item: DefaultEvChargeClaim): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeClaimRequiredLaneRule(var message: String) : DefaultEvChargeClaimRule {
    override fun check(item: DefaultEvChargeClaim): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeClaimRequiredNameRule(var message: String) : DefaultEvChargeClaimRule {
    override fun check(item: DefaultEvChargeClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default ev charge claim. */
data class DefaultEvChargeClaimValidator(var rules: MutableList<DefaultEvChargeClaimRule>) {
    fun validate(item: DefaultEvChargeClaim): MutableList<String> {
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

    fun requireValid(item: DefaultEvChargeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEvChargeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEvChargeClaimValidator(): DefaultEvChargeClaimValidator {
    return DefaultEvChargeClaimValidator(
        mutableListOf<DefaultEvChargeClaimRule>(
            DefaultEvChargeClaimFuelLitersRangeRule(8.6, 3644.6),
            DefaultEvChargeClaimRequiredLaneRule("default ev charge claim needs an entry"),
            DefaultEvChargeClaimRequiredNameRule("default ev charge claim needs an entry"),
        ),
    )
}
