package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.text.normalize

/** A rule a seasonal emission factor claim must pass. Check returns its problems, or none. */
interface SeasonalEmissionFactorClaimRule {
    fun check(item: SeasonalEmissionFactorClaim): MutableList<String>
}

data class SeasonalEmissionFactorClaimAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : SeasonalEmissionFactorClaimRule {
    override fun check(item: SeasonalEmissionFactorClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorClaimRequiredVehicleIdRule(
    var message: String,
) : SeasonalEmissionFactorClaimRule {
    override fun check(item: SeasonalEmissionFactorClaim): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorClaimSourceLengthRule(var maxLength: Int) : SeasonalEmissionFactorClaimRule {
    override fun check(item: SeasonalEmissionFactorClaim): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorClaimRequiredNameRule(var message: String) : SeasonalEmissionFactorClaimRule {
    override fun check(item: SeasonalEmissionFactorClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorClaimVehicleIdLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorClaimRule {
    override fun check(item: SeasonalEmissionFactorClaim): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal emission factor claim. */
data class SeasonalEmissionFactorClaimValidator(var rules: MutableList<SeasonalEmissionFactorClaimRule>) {
    fun validate(item: SeasonalEmissionFactorClaim): MutableList<String> {
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

    fun requireValid(item: SeasonalEmissionFactorClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEmissionFactorClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEmissionFactorClaimValidator(): SeasonalEmissionFactorClaimValidator {
    return SeasonalEmissionFactorClaimValidator(
        mutableListOf<SeasonalEmissionFactorClaimRule>(
            SeasonalEmissionFactorClaimAllowedVehicleIdRule(mutableListOf<String>("strict", "global", "capped")),
            SeasonalEmissionFactorClaimRequiredVehicleIdRule("seasonal emission factor claim needs this field"),
            SeasonalEmissionFactorClaimSourceLengthRule(3600),
            SeasonalEmissionFactorClaimRequiredNameRule("seasonal emission factor claim needs a value"),
            SeasonalEmissionFactorClaimVehicleIdLengthRule(86400),
        ),
    )
}
