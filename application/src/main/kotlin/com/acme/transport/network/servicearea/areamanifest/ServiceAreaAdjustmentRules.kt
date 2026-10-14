package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.text.normalize

/** A rule a service area adjustment must pass. Check returns its problems, or none. */
interface ServiceAreaAdjustmentRule {
    fun check(item: ServiceAreaAdjustment): MutableList<String>
}

data class ServiceAreaAdjustmentExpiresAtRangeRule(var min: Int, var max: Int) : ServiceAreaAdjustmentRule {
    override fun check(item: ServiceAreaAdjustment): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaAdjustmentBudgetRangeRule(var min: Double, var max: Double) : ServiceAreaAdjustmentRule {
    override fun check(item: ServiceAreaAdjustment): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaAdjustmentRequiredTitleRule(var message: String) : ServiceAreaAdjustmentRule {
    override fun check(item: ServiceAreaAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAdjustmentAllowedCodeRule(var allowed: MutableList<String>) : ServiceAreaAdjustmentRule {
    override fun check(item: ServiceAreaAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAdjustmentDiscountRangeRule(
    var min: Double,
    var max: Double,
) : ServiceAreaAdjustmentRule {
    override fun check(item: ServiceAreaAdjustment): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a service area adjustment. */
data class ServiceAreaAdjustmentValidator(var rules: MutableList<ServiceAreaAdjustmentRule>) {
    fun validate(item: ServiceAreaAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaAdjustmentValidator(): ServiceAreaAdjustmentValidator {
    return ServiceAreaAdjustmentValidator(
        mutableListOf<ServiceAreaAdjustmentRule>(
            ServiceAreaAdjustmentExpiresAtRangeRule(64, 1000),
            ServiceAreaAdjustmentBudgetRangeRule(8.9, 2345.4),
            ServiceAreaAdjustmentRequiredTitleRule("service area adjustment needs an entry"),
            ServiceAreaAdjustmentAllowedCodeRule(mutableListOf<String>("legacy")),
            ServiceAreaAdjustmentDiscountRangeRule(3.8, 220.6),
        ),
    )
}
