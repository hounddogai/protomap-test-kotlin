package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.kit.text.normalize

/** A rule a tiered incident override must pass. Check returns its problems, or none. */
interface TieredIncidentOverrideRule {
    fun check(item: TieredIncidentOverride): MutableList<String>
}

data class TieredIncidentOverrideVehicleIdLengthRule(var maxLength: Int) : TieredIncidentOverrideRule {
    override fun check(item: TieredIncidentOverride): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentOverrideNameLengthRule(var maxLength: Int) : TieredIncidentOverrideRule {
    override fun check(item: TieredIncidentOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentOverrideGraceDaysRangeRule(var min: Int, var max: Int) : TieredIncidentOverrideRule {
    override fun check(item: TieredIncidentOverride): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentOverrideAllowedCodeRule(
    var allowed: MutableList<String>,
) : TieredIncidentOverrideRule {
    override fun check(item: TieredIncidentOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentOverrideRequiredCountryRule(var message: String) : TieredIncidentOverrideRule {
    override fun check(item: TieredIncidentOverride): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentOverrideCountryLengthRule(var maxLength: Int) : TieredIncidentOverrideRule {
    override fun check(item: TieredIncidentOverride): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered incident override. */
data class TieredIncidentOverrideValidator(var rules: MutableList<TieredIncidentOverrideRule>) {
    fun validate(item: TieredIncidentOverride): MutableList<String> {
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

    fun requireValid(item: TieredIncidentOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredIncidentOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredIncidentOverrideValidator(): TieredIncidentOverrideValidator {
    return TieredIncidentOverrideValidator(
        mutableListOf<TieredIncidentOverrideRule>(
            TieredIncidentOverrideVehicleIdLengthRule(64),
            TieredIncidentOverrideNameLengthRule(32),
            TieredIncidentOverrideGraceDaysRangeRule(64, 86400),
            TieredIncidentOverrideAllowedCodeRule(mutableListOf<String>("manual", "adaptive", "legacy")),
            TieredIncidentOverrideRequiredCountryRule("tiered incident override needs a value"),
            TieredIncidentOverrideCountryLengthRule(80),
        ),
    )
}
