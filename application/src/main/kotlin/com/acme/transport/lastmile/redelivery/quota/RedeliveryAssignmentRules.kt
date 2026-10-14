package com.acme.transport.lastmile.redelivery.quota

/** A rule a redelivery assignment must pass. Check returns its problems, or none. */
interface RedeliveryAssignmentRule {
    fun check(item: RedeliveryAssignment): MutableList<String>
}

data class RedeliveryAssignmentRequiredChannelRule(var message: String) : RedeliveryAssignmentRule {
    override fun check(item: RedeliveryAssignment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryAssignmentRequiredLabelRule(var message: String) : RedeliveryAssignmentRule {
    override fun check(item: RedeliveryAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryAssignmentChannelLengthRule(var maxLength: Int) : RedeliveryAssignmentRule {
    override fun check(item: RedeliveryAssignment): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryAssignmentAmountRangeRule(var min: Double, var max: Double) : RedeliveryAssignmentRule {
    override fun check(item: RedeliveryAssignment): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class RedeliveryAssignmentDiscountRangeRule(var min: Double, var max: Double) : RedeliveryAssignmentRule {
    override fun check(item: RedeliveryAssignment): MutableList<String> {
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

/** Runs every rule of a redelivery assignment. */
data class RedeliveryAssignmentValidator(var rules: MutableList<RedeliveryAssignmentRule>) {
    fun validate(item: RedeliveryAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryAssignmentValidator(): RedeliveryAssignmentValidator {
    return RedeliveryAssignmentValidator(
        mutableListOf<RedeliveryAssignmentRule>(
            RedeliveryAssignmentRequiredChannelRule("redelivery assignment needs a value"),
            RedeliveryAssignmentRequiredLabelRule("redelivery assignment needs a value"),
            RedeliveryAssignmentChannelLengthRule(120),
            RedeliveryAssignmentAmountRangeRule(2.3, 3304.9),
            RedeliveryAssignmentDiscountRangeRule(1.6, 1942.4),
        ),
    )
}
