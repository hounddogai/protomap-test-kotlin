package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.text.normalize

/** A rule a parcel tier must pass. Check returns its problems, or none. */
interface ParcelTierRule {
    fun check(item: ParcelTier): MutableList<String>
}

data class ParcelTierOdometerRangeRule(var min: Int, var max: Int) : ParcelTierRule {
    override fun check(item: ParcelTier): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelTierAllowedNameRule(var allowed: MutableList<String>) : ParcelTierRule {
    override fun check(item: ParcelTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ParcelTierRequiredCodeRule(var message: String) : ParcelTierRule {
    override fun check(item: ParcelTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelTierVersionRangeRule(var min: Int, var max: Int) : ParcelTierRule {
    override fun check(item: ParcelTier): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a parcel tier. */
data class ParcelTierValidator(var rules: MutableList<ParcelTierRule>) {
    fun validate(item: ParcelTier): MutableList<String> {
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

    fun requireValid(item: ParcelTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelTier>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelTierValidator(): ParcelTierValidator {
    return ParcelTierValidator(
        mutableListOf<ParcelTierRule>(
            ParcelTierOdometerRangeRule(16, 64),
            ParcelTierAllowedNameRule(mutableListOf<String>("rolling", "batched")),
            ParcelTierRequiredCodeRule("parcel tier needs a value"),
            ParcelTierVersionRangeRule(32, 86400),
        ),
    )
}
