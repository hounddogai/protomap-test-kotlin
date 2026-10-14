package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.kit.text.normalize

/** A rule a manual accessorial budget must pass. Check returns its problems, or none. */
interface ManualAccessorialBudgetRule {
    fun check(item: ManualAccessorialBudget): MutableList<String>
}

data class ManualAccessorialBudgetRequiredDriverIdRule(var message: String) : ManualAccessorialBudgetRule {
    override fun check(item: ManualAccessorialBudget): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialBudgetCodePrefixRule(var prefix: String) : ManualAccessorialBudgetRule {
    override fun check(item: ManualAccessorialBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialBudgetAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : ManualAccessorialBudgetRule {
    override fun check(item: ManualAccessorialBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialBudgetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : ManualAccessorialBudgetRule {
    override fun check(item: ManualAccessorialBudget): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class ManualAccessorialBudgetRequiredCodeRule(var message: String) : ManualAccessorialBudgetRule {
    override fun check(item: ManualAccessorialBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialBudgetDriverIdLengthRule(var maxLength: Int) : ManualAccessorialBudgetRule {
    override fun check(item: ManualAccessorialBudget): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual accessorial budget. */
data class ManualAccessorialBudgetValidator(var rules: MutableList<ManualAccessorialBudgetRule>) {
    fun validate(item: ManualAccessorialBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualAccessorialBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAccessorialBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAccessorialBudgetValidator(): ManualAccessorialBudgetValidator {
    return ManualAccessorialBudgetValidator(
        mutableListOf<ManualAccessorialBudgetRule>(
            ManualAccessorialBudgetRequiredDriverIdRule("manual accessorial budget needs this field"),
            ManualAccessorialBudgetCodePrefixRule("OPS"),
            ManualAccessorialBudgetAllowedDriverIdRule(mutableListOf<String>()),
            ManualAccessorialBudgetThresholdRangeRule(7.6, 2504.7),
            ManualAccessorialBudgetRequiredCodeRule("manual accessorial budget needs this field"),
            ManualAccessorialBudgetDriverIdLengthRule(1000),
        ),
    )
}
