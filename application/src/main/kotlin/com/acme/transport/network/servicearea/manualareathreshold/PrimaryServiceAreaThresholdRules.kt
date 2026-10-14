package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.text.normalize

/** A rule a primary service area threshold must pass. Check returns its problems, or none. */
interface PrimaryServiceAreaThresholdRule {
    fun check(item: PrimaryServiceAreaThreshold): MutableList<String>
}

data class PrimaryServiceAreaThresholdRequiredRegionRule(
    var message: String,
) : PrimaryServiceAreaThresholdRule {
    override fun check(item: PrimaryServiceAreaThreshold): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaThresholdAllowedNameRule(
    var allowed: MutableList<String>,
) : PrimaryServiceAreaThresholdRule {
    override fun check(item: PrimaryServiceAreaThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaThresholdMaximumRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryServiceAreaThresholdRule {
    override fun check(item: PrimaryServiceAreaThreshold): MutableList<String> {
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

data class PrimaryServiceAreaThresholdQuantityRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryServiceAreaThresholdRule {
    override fun check(item: PrimaryServiceAreaThreshold): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaThresholdNameLengthRule(var maxLength: Int) : PrimaryServiceAreaThresholdRule {
    override fun check(item: PrimaryServiceAreaThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary service area threshold. */
data class PrimaryServiceAreaThresholdValidator(var rules: MutableList<PrimaryServiceAreaThresholdRule>) {
    fun validate(item: PrimaryServiceAreaThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryServiceAreaThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryServiceAreaThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryServiceAreaThresholdValidator(): PrimaryServiceAreaThresholdValidator {
    return PrimaryServiceAreaThresholdValidator(
        mutableListOf<PrimaryServiceAreaThresholdRule>(
            PrimaryServiceAreaThresholdRequiredRegionRule("primary service area threshold needs an entry"),
            PrimaryServiceAreaThresholdAllowedNameRule(mutableListOf<String>("seasonal")),
            PrimaryServiceAreaThresholdMaximumRangeRule(2.0, 2414.7),
            PrimaryServiceAreaThresholdQuantityRangeRule(80, 255),
            PrimaryServiceAreaThresholdNameLengthRule(3600),
        ),
    )
}
