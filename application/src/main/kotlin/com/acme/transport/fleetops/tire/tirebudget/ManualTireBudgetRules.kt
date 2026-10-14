package com.acme.transport.fleetops.tire.tirebudget

/** A rule a manual tire budget must pass. Check returns its problems, or none. */
interface ManualTireBudgetRule {
    fun check(item: ManualTireBudget): MutableList<String>
}

data class ManualTireBudgetDurationMinutesRangeRule(var min: Int, var max: Int) : ManualTireBudgetRule {
    override fun check(item: ManualTireBudget): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class ManualTireBudgetTrailerIdLengthRule(var maxLength: Int) : ManualTireBudgetRule {
    override fun check(item: ManualTireBudget): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTireBudgetCodePrefixRule(var prefix: String) : ManualTireBudgetRule {
    override fun check(item: ManualTireBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualTireBudgetRequiredCodeRule(var message: String) : ManualTireBudgetRule {
    override fun check(item: ManualTireBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tire budget. */
data class ManualTireBudgetValidator(var rules: MutableList<ManualTireBudgetRule>) {
    fun validate(item: ManualTireBudget): MutableList<String> {
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

    fun requireValid(item: ManualTireBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTireBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTireBudgetValidator(): ManualTireBudgetValidator {
    return ManualTireBudgetValidator(
        mutableListOf<ManualTireBudgetRule>(
            ManualTireBudgetDurationMinutesRangeRule(80, 86400),
            ManualTireBudgetTrailerIdLengthRule(32),
            ManualTireBudgetCodePrefixRule("ACM"),
            ManualTireBudgetRequiredCodeRule("manual tire budget needs a value"),
        ),
    )
}
