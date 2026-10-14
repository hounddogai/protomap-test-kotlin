package com.acme.transport.network.servicearea.areaassignment

/** A rule a service area quota must pass. Check returns its problems, or none. */
interface ServiceAreaQuotaRule {
    fun check(item: ServiceAreaQuota): MutableList<String>
}

data class ServiceAreaQuotaRequiredCategoryRule(var message: String) : ServiceAreaQuotaRule {
    override fun check(item: ServiceAreaQuota): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaQuotaSequenceRangeRule(var min: Int, var max: Int) : ServiceAreaQuotaRule {
    override fun check(item: ServiceAreaQuota): MutableList<String> {
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

data class ServiceAreaQuotaCategoryLengthRule(var maxLength: Int) : ServiceAreaQuotaRule {
    override fun check(item: ServiceAreaQuota): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaQuotaSourceLengthRule(var maxLength: Int) : ServiceAreaQuotaRule {
    override fun check(item: ServiceAreaQuota): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaQuotaCodeLengthRule(var maxLength: Int) : ServiceAreaQuotaRule {
    override fun check(item: ServiceAreaQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area quota. */
data class ServiceAreaQuotaValidator(var rules: MutableList<ServiceAreaQuotaRule>) {
    fun validate(item: ServiceAreaQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaQuotaValidator(): ServiceAreaQuotaValidator {
    return ServiceAreaQuotaValidator(
        mutableListOf<ServiceAreaQuotaRule>(
            ServiceAreaQuotaRequiredCategoryRule("service area quota needs a value"),
            ServiceAreaQuotaSequenceRangeRule(16, 16),
            ServiceAreaQuotaCategoryLengthRule(255),
            ServiceAreaQuotaSourceLengthRule(86400),
            ServiceAreaQuotaCodeLengthRule(1000),
        ),
    )
}
