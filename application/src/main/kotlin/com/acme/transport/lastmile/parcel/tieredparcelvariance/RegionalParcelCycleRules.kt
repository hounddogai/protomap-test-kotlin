package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.text.normalize

/** A rule a regional parcel cycle must pass. Check returns its problems, or none. */
interface RegionalParcelCycleRule {
    fun check(item: RegionalParcelCycle): MutableList<String>
}

data class RegionalParcelCycleNameLengthRule(var maxLength: Int) : RegionalParcelCycleRule {
    override fun check(item: RegionalParcelCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelCycleCodePrefixRule(var prefix: String) : RegionalParcelCycleRule {
    override fun check(item: RegionalParcelCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelCycleRequiredDockRule(var message: String) : RegionalParcelCycleRule {
    override fun check(item: RegionalParcelCycle): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelCycleDistanceKmRangeRule(var min: Double, var max: Double) : RegionalParcelCycleRule {
    override fun check(item: RegionalParcelCycle): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelCycleAllowedNameRule(var allowed: MutableList<String>) : RegionalParcelCycleRule {
    override fun check(item: RegionalParcelCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelCycleCapacityRangeRule(var min: Int, var max: Int) : RegionalParcelCycleRule {
    override fun check(item: RegionalParcelCycle): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional parcel cycle. */
data class RegionalParcelCycleValidator(var rules: MutableList<RegionalParcelCycleRule>) {
    fun validate(item: RegionalParcelCycle): MutableList<String> {
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

    fun requireValid(item: RegionalParcelCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalParcelCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalParcelCycleValidator(): RegionalParcelCycleValidator {
    return RegionalParcelCycleValidator(
        mutableListOf<RegionalParcelCycleRule>(
            RegionalParcelCycleNameLengthRule(255),
            RegionalParcelCycleCodePrefixRule("X"),
            RegionalParcelCycleRequiredDockRule("regional parcel cycle needs a value"),
            RegionalParcelCycleDistanceKmRangeRule(0.8, 151.2),
            RegionalParcelCycleAllowedNameRule(mutableListOf<String>("strict", "global")),
            RegionalParcelCycleCapacityRangeRule(64, 80),
        ),
    )
}
