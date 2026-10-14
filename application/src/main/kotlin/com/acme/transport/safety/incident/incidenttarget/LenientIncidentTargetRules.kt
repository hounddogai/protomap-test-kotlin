package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.text.normalize

/** A rule a lenient incident target must pass. Check returns its problems, or none. */
interface LenientIncidentTargetRule {
    fun check(item: LenientIncidentTarget): MutableList<String>
}

data class LenientIncidentTargetAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : LenientIncidentTargetRule {
    override fun check(item: LenientIncidentTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LenientIncidentTargetMinimumRangeRule(var min: Double, var max: Double) : LenientIncidentTargetRule {
    override fun check(item: LenientIncidentTarget): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class LenientIncidentTargetLaneLengthRule(var maxLength: Int) : LenientIncidentTargetRule {
    override fun check(item: LenientIncidentTarget): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient incident target. */
data class LenientIncidentTargetValidator(var rules: MutableList<LenientIncidentTargetRule>) {
    fun validate(item: LenientIncidentTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientIncidentTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientIncidentTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientIncidentTargetValidator(): LenientIncidentTargetValidator {
    return LenientIncidentTargetValidator(
        mutableListOf<LenientIncidentTargetRule>(
            LenientIncidentTargetAllowedShiftCodeRule(mutableListOf<String>()),
            LenientIncidentTargetMinimumRangeRule(6.2, 4180.6),
            LenientIncidentTargetLaneLengthRule(86400),
        ),
    )
}
