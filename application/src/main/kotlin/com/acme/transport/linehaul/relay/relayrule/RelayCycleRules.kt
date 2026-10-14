package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.text.normalize

/** A rule a relay cycle must pass. Check returns its problems, or none. */
interface RelayCycleRule {
    fun check(item: RelayCycle): MutableList<String>
}

data class RelayCycleMaximumRangeRule(var min: Double, var max: Double) : RelayCycleRule {
    override fun check(item: RelayCycle): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class RelayCycleRequiredCodeRule(var message: String) : RelayCycleRule {
    override fun check(item: RelayCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayCycleAllowedLabelRule(var allowed: MutableList<String>) : RelayCycleRule {
    override fun check(item: RelayCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay cycle. */
data class RelayCycleValidator(var rules: MutableList<RelayCycleRule>) {
    fun validate(item: RelayCycle): MutableList<String> {
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

    fun requireValid(item: RelayCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayCycleValidator(): RelayCycleValidator {
    return RelayCycleValidator(
        mutableListOf<RelayCycleRule>(
            RelayCycleMaximumRangeRule(1.4, 3648.0),
            RelayCycleRequiredCodeRule("relay cycle needs a value"),
            RelayCycleAllowedLabelRule(mutableListOf<String>("capped", "default", "seasonal")),
        ),
    )
}
