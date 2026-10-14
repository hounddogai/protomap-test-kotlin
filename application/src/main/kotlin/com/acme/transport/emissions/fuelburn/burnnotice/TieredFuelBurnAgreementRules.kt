package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.kit.text.normalize

/** A rule a tiered fuel burn agreement must pass. Check returns its problems, or none. */
interface TieredFuelBurnAgreementRule {
    fun check(item: TieredFuelBurnAgreement): MutableList<String>
}

data class TieredFuelBurnAgreementRequiredDriverIdRule(var message: String) : TieredFuelBurnAgreementRule {
    override fun check(item: TieredFuelBurnAgreement): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnAgreementLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : TieredFuelBurnAgreementRule {
    override fun check(item: TieredFuelBurnAgreement): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class TieredFuelBurnAgreementAllowedNameRule(
    var allowed: MutableList<String>,
) : TieredFuelBurnAgreementRule {
    override fun check(item: TieredFuelBurnAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnAgreementRequiredCodeRule(var message: String) : TieredFuelBurnAgreementRule {
    override fun check(item: TieredFuelBurnAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnAgreementMaximumRangeRule(
    var min: Double,
    var max: Double,
) : TieredFuelBurnAgreementRule {
    override fun check(item: TieredFuelBurnAgreement): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnAgreementRequiredVehicleIdRule(var message: String) : TieredFuelBurnAgreementRule {
    override fun check(item: TieredFuelBurnAgreement): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel burn agreement. */
data class TieredFuelBurnAgreementValidator(var rules: MutableList<TieredFuelBurnAgreementRule>) {
    fun validate(item: TieredFuelBurnAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredFuelBurnAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelBurnAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelBurnAgreementValidator(): TieredFuelBurnAgreementValidator {
    return TieredFuelBurnAgreementValidator(
        mutableListOf<TieredFuelBurnAgreementRule>(
            TieredFuelBurnAgreementRequiredDriverIdRule("tiered fuel burn agreement needs an entry"),
            TieredFuelBurnAgreementLoadKgRangeRule(4.0, 4948.2),
            TieredFuelBurnAgreementAllowedNameRule(mutableListOf<String>("regional", "lenient", "scheduled")),
            TieredFuelBurnAgreementRequiredCodeRule("tiered fuel burn agreement needs an entry"),
            TieredFuelBurnAgreementMaximumRangeRule(5.8, 562.6),
            TieredFuelBurnAgreementRequiredVehicleIdRule("tiered fuel burn agreement needs an entry"),
        ),
    )
}
