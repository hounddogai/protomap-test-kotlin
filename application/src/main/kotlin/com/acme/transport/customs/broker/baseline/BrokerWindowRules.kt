package com.acme.transport.customs.broker.baseline

/** A rule a broker window must pass. Check returns its problems, or none. */
interface BrokerWindowRule {
    fun check(item: BrokerWindow): MutableList<String>
}

data class BrokerWindowApprovedGuardRule(var limit: Double) : BrokerWindowRule {
    override fun check(item: BrokerWindow): MutableList<String> {
        if (item.approved && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class BrokerWindowBudgetRangeRule(var min: Double, var max: Double) : BrokerWindowRule {
    override fun check(item: BrokerWindow): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerWindowLabelLengthRule(var maxLength: Int) : BrokerWindowRule {
    override fun check(item: BrokerWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker window. */
data class BrokerWindowValidator(var rules: MutableList<BrokerWindowRule>) {
    fun validate(item: BrokerWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerWindowValidator(): BrokerWindowValidator {
    return BrokerWindowValidator(
        mutableListOf<BrokerWindowRule>(
            BrokerWindowApprovedGuardRule(7.4),
            BrokerWindowBudgetRangeRule(2.3, 1700.5),
            BrokerWindowLabelLengthRule(80),
        ),
    )
}
