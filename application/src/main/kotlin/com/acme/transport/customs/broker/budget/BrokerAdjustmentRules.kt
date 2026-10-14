package com.acme.transport.customs.broker.budget

/** A rule a broker adjustment must pass. Check returns its problems, or none. */
interface BrokerAdjustmentRule {
    fun check(item: BrokerAdjustment): MutableList<String>
}

data class BrokerAdjustmentPinnedGuardRule(var limit: Double) : BrokerAdjustmentRule {
    override fun check(item: BrokerAdjustment): MutableList<String> {
        if (item.pinned && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class BrokerAdjustmentCodeLengthRule(var maxLength: Int) : BrokerAdjustmentRule {
    override fun check(item: BrokerAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerAdjustmentRequiredCodeRule(var message: String) : BrokerAdjustmentRule {
    override fun check(item: BrokerAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerAdjustmentEndsAtRangeRule(var min: Int, var max: Int) : BrokerAdjustmentRule {
    override fun check(item: BrokerAdjustment): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker adjustment. */
data class BrokerAdjustmentValidator(var rules: MutableList<BrokerAdjustmentRule>) {
    fun validate(item: BrokerAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerAdjustmentValidator(): BrokerAdjustmentValidator {
    return BrokerAdjustmentValidator(
        mutableListOf<BrokerAdjustmentRule>(
            BrokerAdjustmentPinnedGuardRule(8.2),
            BrokerAdjustmentCodeLengthRule(64),
            BrokerAdjustmentRequiredCodeRule("broker adjustment needs an entry"),
            BrokerAdjustmentEndsAtRangeRule(120, 255),
        ),
    )
}
