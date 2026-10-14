package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.normalize

/** A rule a service plan estimate must pass. Check returns its problems, or none. */
interface ServicePlanEstimateRule {
    fun check(item: ServicePlanEstimate): MutableList<String>
}

data class ServicePlanEstimateTitleLengthRule(var maxLength: Int) : ServicePlanEstimateRule {
    override fun check(item: ServicePlanEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanEstimateCodeLengthRule(var maxLength: Int) : ServicePlanEstimateRule {
    override fun check(item: ServicePlanEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanEstimateCodePrefixRule(var prefix: String) : ServicePlanEstimateRule {
    override fun check(item: ServicePlanEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanEstimateLimitRangeRule(var min: Int, var max: Int) : ServicePlanEstimateRule {
    override fun check(item: ServicePlanEstimate): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanEstimateAllowedTitleRule(var allowed: MutableList<String>) : ServicePlanEstimateRule {
    override fun check(item: ServicePlanEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanEstimateRequiredTitleRule(var message: String) : ServicePlanEstimateRule {
    override fun check(item: ServicePlanEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan estimate. */
data class ServicePlanEstimateValidator(var rules: MutableList<ServicePlanEstimateRule>) {
    fun validate(item: ServicePlanEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanEstimateValidator(): ServicePlanEstimateValidator {
    return ServicePlanEstimateValidator(
        mutableListOf<ServicePlanEstimateRule>(
            ServicePlanEstimateTitleLengthRule(3600),
            ServicePlanEstimateCodeLengthRule(64),
            ServicePlanEstimateCodePrefixRule("OPS"),
            ServicePlanEstimateLimitRangeRule(32, 1000),
            ServicePlanEstimateAllowedTitleRule(mutableListOf<String>("primary")),
            ServicePlanEstimateRequiredTitleRule("service plan estimate needs a value"),
        ),
    )
}
