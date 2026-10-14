package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.text.normalize

/** A rule a backhaul cycle must pass. Check returns its problems, or none. */
interface BackhaulCycleRule {
    fun check(item: BackhaulCycle): MutableList<String>
}

data class BackhaulCycleAllowedCodeRule(var allowed: MutableList<String>) : BackhaulCycleRule {
    override fun check(item: BackhaulCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulCycleRatioRangeRule(var min: Double, var max: Double) : BackhaulCycleRule {
    override fun check(item: BackhaulCycle): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulCycleSequenceRangeRule(var min: Int, var max: Int) : BackhaulCycleRule {
    override fun check(item: BackhaulCycle): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class BackhaulCycleApprovedGuardRule(var limit: Int) : BackhaulCycleRule {
    override fun check(item: BackhaulCycle): MutableList<String> {
        if (item.approved && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulCycleRegionLengthRule(var maxLength: Int) : BackhaulCycleRule {
    override fun check(item: BackhaulCycle): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulCycleCodeLengthRule(var maxLength: Int) : BackhaulCycleRule {
    override fun check(item: BackhaulCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul cycle. */
data class BackhaulCycleValidator(var rules: MutableList<BackhaulCycleRule>) {
    fun validate(item: BackhaulCycle): MutableList<String> {
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

    fun requireValid(item: BackhaulCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulCycleValidator(): BackhaulCycleValidator {
    return BackhaulCycleValidator(
        mutableListOf<BackhaulCycleRule>(
            BackhaulCycleAllowedCodeRule(mutableListOf<String>()),
            BackhaulCycleRatioRangeRule(8.3, 4360.7),
            BackhaulCycleSequenceRangeRule(16, 120),
            BackhaulCycleApprovedGuardRule(16),
            BackhaulCycleRegionLengthRule(1000),
            BackhaulCycleCodeLengthRule(120),
        ),
    )
}
