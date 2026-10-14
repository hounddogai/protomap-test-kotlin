package com.acme.transport.freightpay.accessorial.score

/** A rule a pending accessorial budget must pass. Check returns its problems, or none. */
interface PendingAccessorialBudgetRule {
    fun check(item: PendingAccessorialBudget): MutableList<String>
}

data class PendingAccessorialBudgetLaneLengthRule(var maxLength: Int) : PendingAccessorialBudgetRule {
    override fun check(item: PendingAccessorialBudget): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialBudgetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : PendingAccessorialBudgetRule {
    override fun check(item: PendingAccessorialBudget): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialBudgetPriorityRangeRule(
    var min: Int,
    var max: Int,
) : PendingAccessorialBudgetRule {
    override fun check(item: PendingAccessorialBudget): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialBudgetRequiredNameRule(var message: String) : PendingAccessorialBudgetRule {
    override fun check(item: PendingAccessorialBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialBudgetQuantityRangeRule(
    var min: Int,
    var max: Int,
) : PendingAccessorialBudgetRule {
    override fun check(item: PendingAccessorialBudget): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending accessorial budget. */
data class PendingAccessorialBudgetValidator(var rules: MutableList<PendingAccessorialBudgetRule>) {
    fun validate(item: PendingAccessorialBudget): MutableList<String> {
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

    fun requireValid(item: PendingAccessorialBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAccessorialBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAccessorialBudgetValidator(): PendingAccessorialBudgetValidator {
    return PendingAccessorialBudgetValidator(
        mutableListOf<PendingAccessorialBudgetRule>(
            PendingAccessorialBudgetLaneLengthRule(64),
            PendingAccessorialBudgetThresholdRangeRule(8.1, 4240.8),
            PendingAccessorialBudgetPriorityRangeRule(120, 3600),
            PendingAccessorialBudgetRequiredNameRule("pending accessorial budget needs a value"),
            PendingAccessorialBudgetQuantityRangeRule(120, 255),
        ),
    )
}
