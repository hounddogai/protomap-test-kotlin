package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.text.normalize

/** A rule a rolling zone window must pass. Check returns its problems, or none. */
interface RollingZoneWindowRule {
    fun check(item: RollingZoneWindow): MutableList<String>
}

data class RollingZoneWindowExternalRefLengthRule(var maxLength: Int) : RollingZoneWindowRule {
    override fun check(item: RollingZoneWindow): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneWindowCapacityRangeRule(var min: Int, var max: Int) : RollingZoneWindowRule {
    override fun check(item: RollingZoneWindow): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneWindowMinimumRangeRule(var min: Double, var max: Double) : RollingZoneWindowRule {
    override fun check(item: RollingZoneWindow): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneWindowAllowedContactEmailRule(var allowed: MutableList<String>) : RollingZoneWindowRule {
    override fun check(item: RollingZoneWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling zone window. */
data class RollingZoneWindowValidator(var rules: MutableList<RollingZoneWindowRule>) {
    fun validate(item: RollingZoneWindow): MutableList<String> {
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

    fun requireValid(item: RollingZoneWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingZoneWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingZoneWindowValidator(): RollingZoneWindowValidator {
    return RollingZoneWindowValidator(
        mutableListOf<RollingZoneWindowRule>(
            RollingZoneWindowExternalRefLengthRule(32),
            RollingZoneWindowCapacityRangeRule(16, 32),
            RollingZoneWindowMinimumRangeRule(8.3, 1169.7),
            RollingZoneWindowAllowedContactEmailRule(mutableListOf<String>("pending")),
        ),
    )
}
