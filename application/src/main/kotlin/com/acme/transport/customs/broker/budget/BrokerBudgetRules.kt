package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.normalize

/** A rule a broker budget must pass. Check returns its problems, or none. */
interface BrokerBudgetRule {
    fun check(item: BrokerBudget): MutableList<String>
}

data class BrokerBudgetAllowedTitleRule(var allowed: MutableList<String>) : BrokerBudgetRule {
    override fun check(item: BrokerBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BrokerBudgetMarginRangeRule(var min: Double, var max: Double) : BrokerBudgetRule {
    override fun check(item: BrokerBudget): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class BrokerBudgetRequiredNotesRule(var message: String) : BrokerBudgetRule {
    override fun check(item: BrokerBudget): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerBudgetVehicleIdLengthRule(var maxLength: Int) : BrokerBudgetRule {
    override fun check(item: BrokerBudget): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerBudgetNotesLengthRule(var maxLength: Int) : BrokerBudgetRule {
    override fun check(item: BrokerBudget): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker budget. */
data class BrokerBudgetValidator(var rules: MutableList<BrokerBudgetRule>) {
    fun validate(item: BrokerBudget): MutableList<String> {
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

    fun requireValid(item: BrokerBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerBudgetValidator(): BrokerBudgetValidator {
    return BrokerBudgetValidator(
        mutableListOf<BrokerBudgetRule>(
            BrokerBudgetAllowedTitleRule(mutableListOf<String>()),
            BrokerBudgetMarginRangeRule(0.7, 2308.7),
            BrokerBudgetRequiredNotesRule("broker budget needs an entry"),
            BrokerBudgetVehicleIdLengthRule(32),
            BrokerBudgetNotesLengthRule(3600),
        ),
    )
}
