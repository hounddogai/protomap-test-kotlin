package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.text.normalize

/** A rule a near miss cycle must pass. Check returns its problems, or none. */
interface NearMissCycleRule {
    fun check(item: NearMissCycle): MutableList<String>
}

data class NearMissCycleStartsAtRangeRule(var min: Int, var max: Int) : NearMissCycleRule {
    override fun check(item: NearMissCycle): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissCycleCodePrefixRule(var prefix: String) : NearMissCycleRule {
    override fun check(item: NearMissCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissCycleLimitRangeRule(var min: Int, var max: Int) : NearMissCycleRule {
    override fun check(item: NearMissCycle): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class NearMissCycleRequiredLabelRule(var message: String) : NearMissCycleRule {
    override fun check(item: NearMissCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissCycleAllowedVehicleIdRule(var allowed: MutableList<String>) : NearMissCycleRule {
    override fun check(item: NearMissCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class NearMissCycleLabelLengthRule(var maxLength: Int) : NearMissCycleRule {
    override fun check(item: NearMissCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss cycle. */
data class NearMissCycleValidator(var rules: MutableList<NearMissCycleRule>) {
    fun validate(item: NearMissCycle): MutableList<String> {
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

    fun requireValid(item: NearMissCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissCycleValidator(): NearMissCycleValidator {
    return NearMissCycleValidator(
        mutableListOf<NearMissCycleRule>(
            NearMissCycleStartsAtRangeRule(32, 64),
            NearMissCycleCodePrefixRule("REF"),
            NearMissCycleLimitRangeRule(80, 86400),
            NearMissCycleRequiredLabelRule("near miss cycle needs an entry"),
            NearMissCycleAllowedVehicleIdRule(mutableListOf<String>()),
            NearMissCycleLabelLengthRule(3600),
        ),
    )
}
