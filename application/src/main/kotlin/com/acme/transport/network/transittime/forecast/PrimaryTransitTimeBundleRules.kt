package com.acme.transport.network.transittime.forecast

/** A rule a primary transit time bundle must pass. Check returns its problems, or none. */
interface PrimaryTransitTimeBundleRule {
    fun check(item: PrimaryTransitTimeBundle): MutableList<String>
}

data class PrimaryTransitTimeBundleFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryTransitTimeBundleRule {
    override fun check(item: PrimaryTransitTimeBundle): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeBundleRequiredRouteCodeRule(var message: String) : PrimaryTransitTimeBundleRule {
    override fun check(item: PrimaryTransitTimeBundle): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeBundleRequiredNameRule(var message: String) : PrimaryTransitTimeBundleRule {
    override fun check(item: PrimaryTransitTimeBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeBundleCodePrefixRule(var prefix: String) : PrimaryTransitTimeBundleRule {
    override fun check(item: PrimaryTransitTimeBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary transit time bundle. */
data class PrimaryTransitTimeBundleValidator(var rules: MutableList<PrimaryTransitTimeBundleRule>) {
    fun validate(item: PrimaryTransitTimeBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTransitTimeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTransitTimeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTransitTimeBundleValidator(): PrimaryTransitTimeBundleValidator {
    return PrimaryTransitTimeBundleValidator(
        mutableListOf<PrimaryTransitTimeBundleRule>(
            PrimaryTransitTimeBundleFloorValueRangeRule(0.2, 2664.0),
            PrimaryTransitTimeBundleRequiredRouteCodeRule("primary transit time bundle needs a value"),
            PrimaryTransitTimeBundleRequiredNameRule("primary transit time bundle needs a value"),
            PrimaryTransitTimeBundleCodePrefixRule("OPS"),
        ),
    )
}
