package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.text.normalize

/** A rule a pending service area bundle must pass. Check returns its problems, or none. */
interface PendingServiceAreaBundleRule {
    fun check(item: PendingServiceAreaBundle): MutableList<String>
}

data class PendingServiceAreaBundleMarginRangeRule(
    var min: Double,
    var max: Double,
) : PendingServiceAreaBundleRule {
    override fun check(item: PendingServiceAreaBundle): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class PendingServiceAreaBundleCodePrefixRule(var prefix: String) : PendingServiceAreaBundleRule {
    override fun check(item: PendingServiceAreaBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaBundleAllowedRegionRule(
    var allowed: MutableList<String>,
) : PendingServiceAreaBundleRule {
    override fun check(item: PendingServiceAreaBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaBundleExternalRefLengthRule(var maxLength: Int) : PendingServiceAreaBundleRule {
    override fun check(item: PendingServiceAreaBundle): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaBundleScoreRangeRule(
    var min: Double,
    var max: Double,
) : PendingServiceAreaBundleRule {
    override fun check(item: PendingServiceAreaBundle): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending service area bundle. */
data class PendingServiceAreaBundleValidator(var rules: MutableList<PendingServiceAreaBundleRule>) {
    fun validate(item: PendingServiceAreaBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingServiceAreaBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingServiceAreaBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingServiceAreaBundleValidator(): PendingServiceAreaBundleValidator {
    return PendingServiceAreaBundleValidator(
        mutableListOf<PendingServiceAreaBundleRule>(
            PendingServiceAreaBundleMarginRangeRule(8.2, 2239.6),
            PendingServiceAreaBundleCodePrefixRule("OPS"),
            PendingServiceAreaBundleAllowedRegionRule(mutableListOf<String>("tiered", "strict", "default")),
            PendingServiceAreaBundleExternalRefLengthRule(16),
            PendingServiceAreaBundleScoreRangeRule(8.2, 618.9),
        ),
    )
}
