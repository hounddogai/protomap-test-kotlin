package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.text.normalize

/** A rule a service area budget must pass. Check returns its problems, or none. */
interface ServiceAreaBudgetRule {
    fun check(item: ServiceAreaBudget): MutableList<String>
}

data class ServiceAreaBudgetRequiredCodeRule(var message: String) : ServiceAreaBudgetRule {
    override fun check(item: ServiceAreaBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBudgetAllowedDescriptionRule(var allowed: MutableList<String>) : ServiceAreaBudgetRule {
    override fun check(item: ServiceAreaBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBudgetDescriptionLengthRule(var maxLength: Int) : ServiceAreaBudgetRule {
    override fun check(item: ServiceAreaBudget): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBudgetCodeLengthRule(var maxLength: Int) : ServiceAreaBudgetRule {
    override fun check(item: ServiceAreaBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBudgetTrailerIdLengthRule(var maxLength: Int) : ServiceAreaBudgetRule {
    override fun check(item: ServiceAreaBudget): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area budget. */
data class ServiceAreaBudgetValidator(var rules: MutableList<ServiceAreaBudgetRule>) {
    fun validate(item: ServiceAreaBudget): MutableList<String> {
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

    fun requireValid(item: ServiceAreaBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaBudgetValidator(): ServiceAreaBudgetValidator {
    return ServiceAreaBudgetValidator(
        mutableListOf<ServiceAreaBudgetRule>(
            ServiceAreaBudgetRequiredCodeRule("service area budget needs an entry"),
            ServiceAreaBudgetAllowedDescriptionRule(mutableListOf<String>("capped")),
            ServiceAreaBudgetDescriptionLengthRule(64),
            ServiceAreaBudgetCodeLengthRule(120),
            ServiceAreaBudgetTrailerIdLengthRule(86400),
        ),
    )
}
