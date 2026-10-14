package com.acme.transport.network.servicearea.summary

/** A rule a service area variance must pass. Check returns its problems, or none. */
interface ServiceAreaVarianceRule {
    fun check(item: ServiceAreaVariance): MutableList<String>
}

data class ServiceAreaVarianceRateRangeRule(var min: Double, var max: Double) : ServiceAreaVarianceRule {
    override fun check(item: ServiceAreaVariance): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaVariancePinnedGuardRule(var limit: Double) : ServiceAreaVarianceRule {
    override fun check(item: ServiceAreaVariance): MutableList<String> {
        if (item.pinned && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaVarianceNameLengthRule(var maxLength: Int) : ServiceAreaVarianceRule {
    override fun check(item: ServiceAreaVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area variance. */
data class ServiceAreaVarianceValidator(var rules: MutableList<ServiceAreaVarianceRule>) {
    fun validate(item: ServiceAreaVariance): MutableList<String> {
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

    fun requireValid(item: ServiceAreaVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaVarianceValidator(): ServiceAreaVarianceValidator {
    return ServiceAreaVarianceValidator(
        mutableListOf<ServiceAreaVarianceRule>(
            ServiceAreaVarianceRateRangeRule(4.9, 1456.6),
            ServiceAreaVariancePinnedGuardRule(8.3),
            ServiceAreaVarianceNameLengthRule(32),
        ),
    )
}
