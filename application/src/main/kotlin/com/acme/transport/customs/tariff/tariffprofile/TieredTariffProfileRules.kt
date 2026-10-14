package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.text.normalize

/** A rule a tiered tariff profile must pass. Check returns its problems, or none. */
interface TieredTariffProfileRule {
    fun check(item: TieredTariffProfile): MutableList<String>
}

data class TieredTariffProfileMarginRangeRule(var min: Double, var max: Double) : TieredTariffProfileRule {
    override fun check(item: TieredTariffProfile): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class TieredTariffProfileDistanceKmRangeRule(var min: Double, var max: Double) : TieredTariffProfileRule {
    override fun check(item: TieredTariffProfile): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class TieredTariffProfileNameLengthRule(var maxLength: Int) : TieredTariffProfileRule {
    override fun check(item: TieredTariffProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTariffProfileCodePrefixRule(var prefix: String) : TieredTariffProfileRule {
    override fun check(item: TieredTariffProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTariffProfileEtaMinutesRangeRule(var min: Int, var max: Int) : TieredTariffProfileRule {
    override fun check(item: TieredTariffProfile): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class TieredTariffProfileAllowedLabelRule(var allowed: MutableList<String>) : TieredTariffProfileRule {
    override fun check(item: TieredTariffProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tariff profile. */
data class TieredTariffProfileValidator(var rules: MutableList<TieredTariffProfileRule>) {
    fun validate(item: TieredTariffProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredTariffProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTariffProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTariffProfileValidator(): TieredTariffProfileValidator {
    return TieredTariffProfileValidator(
        mutableListOf<TieredTariffProfileRule>(
            TieredTariffProfileMarginRangeRule(2.6, 446.2),
            TieredTariffProfileDistanceKmRangeRule(1.2, 3273.2),
            TieredTariffProfileNameLengthRule(16),
            TieredTariffProfileCodePrefixRule("ACM"),
            TieredTariffProfileEtaMinutesRangeRule(80, 120),
            TieredTariffProfileAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
