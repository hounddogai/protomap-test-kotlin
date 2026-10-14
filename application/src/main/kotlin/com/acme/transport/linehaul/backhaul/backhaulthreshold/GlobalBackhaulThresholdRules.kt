package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.normalize

/** A rule a global backhaul threshold must pass. Check returns its problems, or none. */
interface GlobalBackhaulThresholdRule {
    fun check(item: GlobalBackhaulThreshold): MutableList<String>
}

data class GlobalBackhaulThresholdOdometerRangeRule(var min: Int, var max: Int) : GlobalBackhaulThresholdRule {
    override fun check(item: GlobalBackhaulThreshold): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBackhaulThresholdDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : GlobalBackhaulThresholdRule {
    override fun check(item: GlobalBackhaulThreshold): MutableList<String> {
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

data class GlobalBackhaulThresholdCreatedAtRangeRule(var min: Int, var max: Int) : GlobalBackhaulThresholdRule {
    override fun check(item: GlobalBackhaulThreshold): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class GlobalBackhaulThresholdRequiredCategoryRule(var message: String) : GlobalBackhaulThresholdRule {
    override fun check(item: GlobalBackhaulThreshold): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalBackhaulThresholdAllowedCategoryRule(
    var allowed: MutableList<String>,
) : GlobalBackhaulThresholdRule {
    override fun check(item: GlobalBackhaulThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global backhaul threshold. */
data class GlobalBackhaulThresholdValidator(var rules: MutableList<GlobalBackhaulThresholdRule>) {
    fun validate(item: GlobalBackhaulThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalBackhaulThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalBackhaulThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalBackhaulThresholdValidator(): GlobalBackhaulThresholdValidator {
    return GlobalBackhaulThresholdValidator(
        mutableListOf<GlobalBackhaulThresholdRule>(
            GlobalBackhaulThresholdOdometerRangeRule(120, 86400),
            GlobalBackhaulThresholdDistanceKmRangeRule(0.4, 2897.0),
            GlobalBackhaulThresholdCreatedAtRangeRule(120, 255),
            GlobalBackhaulThresholdRequiredCategoryRule("global backhaul threshold needs a value"),
            GlobalBackhaulThresholdAllowedCategoryRule(mutableListOf<String>("manual")),
        ),
    )
}
