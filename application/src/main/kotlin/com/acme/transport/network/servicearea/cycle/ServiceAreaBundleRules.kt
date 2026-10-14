package com.acme.transport.network.servicearea.cycle

/** A rule a service area bundle must pass. Check returns its problems, or none. */
interface ServiceAreaBundleRule {
    fun check(item: ServiceAreaBundle): MutableList<String>
}

data class ServiceAreaBundleRatioRangeRule(var min: Double, var max: Double) : ServiceAreaBundleRule {
    override fun check(item: ServiceAreaBundle): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBundleRequiredTitleRule(var message: String) : ServiceAreaBundleRule {
    override fun check(item: ServiceAreaBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBundleCodePrefixRule(var prefix: String) : ServiceAreaBundleRule {
    override fun check(item: ServiceAreaBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBundleRequiredCodeRule(var message: String) : ServiceAreaBundleRule {
    override fun check(item: ServiceAreaBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area bundle. */
data class ServiceAreaBundleValidator(var rules: MutableList<ServiceAreaBundleRule>) {
    fun validate(item: ServiceAreaBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaBundleValidator(): ServiceAreaBundleValidator {
    return ServiceAreaBundleValidator(
        mutableListOf<ServiceAreaBundleRule>(
            ServiceAreaBundleRatioRangeRule(5.4, 1689.1),
            ServiceAreaBundleRequiredTitleRule("service area bundle needs an entry"),
            ServiceAreaBundleCodePrefixRule("OPS"),
            ServiceAreaBundleRequiredCodeRule("service area bundle needs a value"),
        ),
    )
}
