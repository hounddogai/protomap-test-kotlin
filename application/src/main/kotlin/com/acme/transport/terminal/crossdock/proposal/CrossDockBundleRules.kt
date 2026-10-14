package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.text.normalize

/** A rule a cross dock bundle must pass. Check returns its problems, or none. */
interface CrossDockBundleRule {
    fun check(item: CrossDockBundle): MutableList<String>
}

data class CrossDockBundleCodePrefixRule(var prefix: String) : CrossDockBundleRule {
    override fun check(item: CrossDockBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockBundleFloorValueRangeRule(var min: Double, var max: Double) : CrossDockBundleRule {
    override fun check(item: CrossDockBundle): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class CrossDockBundleAllowedSegmentRule(var allowed: MutableList<String>) : CrossDockBundleRule {
    override fun check(item: CrossDockBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockBundleRatioRangeRule(var min: Double, var max: Double) : CrossDockBundleRule {
    override fun check(item: CrossDockBundle): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class CrossDockBundleRequiredTitleRule(var message: String) : CrossDockBundleRule {
    override fun check(item: CrossDockBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock bundle. */
data class CrossDockBundleValidator(var rules: MutableList<CrossDockBundleRule>) {
    fun validate(item: CrossDockBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockBundleValidator(): CrossDockBundleValidator {
    return CrossDockBundleValidator(
        mutableListOf<CrossDockBundleRule>(
            CrossDockBundleCodePrefixRule("ACM"),
            CrossDockBundleFloorValueRangeRule(5.9, 459.8),
            CrossDockBundleAllowedSegmentRule(mutableListOf<String>("weighted", "seasonal", "fallback")),
            CrossDockBundleRatioRangeRule(6.2, 1736.9),
            CrossDockBundleRequiredTitleRule("cross dock bundle needs a value"),
        ),
    )
}
