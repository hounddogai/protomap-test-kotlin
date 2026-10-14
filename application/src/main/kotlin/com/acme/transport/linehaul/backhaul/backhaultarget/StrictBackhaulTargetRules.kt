package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.text.normalize

/** A rule a strict backhaul target must pass. Check returns its problems, or none. */
interface StrictBackhaulTargetRule {
    fun check(item: StrictBackhaulTarget): MutableList<String>
}

data class StrictBackhaulTargetAllowedCodeRule(var allowed: MutableList<String>) : StrictBackhaulTargetRule {
    override fun check(item: StrictBackhaulTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulTargetTitleLengthRule(var maxLength: Int) : StrictBackhaulTargetRule {
    override fun check(item: StrictBackhaulTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulTargetCapacityRangeRule(var min: Int, var max: Int) : StrictBackhaulTargetRule {
    override fun check(item: StrictBackhaulTarget): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict backhaul target. */
data class StrictBackhaulTargetValidator(var rules: MutableList<StrictBackhaulTargetRule>) {
    fun validate(item: StrictBackhaulTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictBackhaulTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBackhaulTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBackhaulTargetValidator(): StrictBackhaulTargetValidator {
    return StrictBackhaulTargetValidator(
        mutableListOf<StrictBackhaulTargetRule>(
            StrictBackhaulTargetAllowedCodeRule(mutableListOf<String>("adaptive", "capped")),
            StrictBackhaulTargetTitleLengthRule(80),
            StrictBackhaulTargetCapacityRangeRule(1000, 1000),
        ),
    )
}
