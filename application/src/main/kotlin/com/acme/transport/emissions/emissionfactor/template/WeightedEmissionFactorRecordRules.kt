package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.text.normalize

/** A rule a weighted emission factor record must pass. Check returns its problems, or none. */
interface WeightedEmissionFactorRecordRule {
    fun check(item: WeightedEmissionFactorRecord): MutableList<String>
}

data class WeightedEmissionFactorRecordAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : WeightedEmissionFactorRecordRule {
    override fun check(item: WeightedEmissionFactorRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorRecordRequiredVehicleIdRule(
    var message: String,
) : WeightedEmissionFactorRecordRule {
    override fun check(item: WeightedEmissionFactorRecord): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorRecordCodePrefixRule(var prefix: String) : WeightedEmissionFactorRecordRule {
    override fun check(item: WeightedEmissionFactorRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorRecordDiscountRangeRule(
    var min: Double,
    var max: Double,
) : WeightedEmissionFactorRecordRule {
    override fun check(item: WeightedEmissionFactorRecord): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class WeightedEmissionFactorRecordRatioRangeRule(
    var min: Double,
    var max: Double,
) : WeightedEmissionFactorRecordRule {
    override fun check(item: WeightedEmissionFactorRecord): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted emission factor record. */
data class WeightedEmissionFactorRecordValidator(var rules: MutableList<WeightedEmissionFactorRecordRule>) {
    fun validate(item: WeightedEmissionFactorRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedEmissionFactorRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEmissionFactorRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEmissionFactorRecordValidator(): WeightedEmissionFactorRecordValidator {
    return WeightedEmissionFactorRecordValidator(
        mutableListOf<WeightedEmissionFactorRecordRule>(
            WeightedEmissionFactorRecordAllowedVehicleIdRule(
                mutableListOf<String>("manual", "scheduled", "manual"),
            ),
            WeightedEmissionFactorRecordRequiredVehicleIdRule("weighted emission factor record needs a value"),
            WeightedEmissionFactorRecordCodePrefixRule("ACM"),
            WeightedEmissionFactorRecordDiscountRangeRule(8.2, 2748.6),
            WeightedEmissionFactorRecordRatioRangeRule(7.3, 886.1),
        ),
    )
}
