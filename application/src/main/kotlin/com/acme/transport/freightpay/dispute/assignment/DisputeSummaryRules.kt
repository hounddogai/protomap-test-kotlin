package com.acme.transport.freightpay.dispute.assignment

/** A rule a dispute summary must pass. Check returns its problems, or none. */
interface DisputeSummaryRule {
    fun check(item: DisputeSummary): MutableList<String>
}

data class DisputeSummaryDriverIdLengthRule(var maxLength: Int) : DisputeSummaryRule {
    override fun check(item: DisputeSummary): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeSummaryCodeLengthRule(var maxLength: Int) : DisputeSummaryRule {
    override fun check(item: DisputeSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeSummaryCapacityRangeRule(var min: Int, var max: Int) : DisputeSummaryRule {
    override fun check(item: DisputeSummary): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeSummaryDiscountRangeRule(var min: Double, var max: Double) : DisputeSummaryRule {
    override fun check(item: DisputeSummary): MutableList<String> {
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

/** Runs every rule of a dispute summary. */
data class DisputeSummaryValidator(var rules: MutableList<DisputeSummaryRule>) {
    fun validate(item: DisputeSummary): MutableList<String> {
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

    fun requireValid(item: DisputeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeSummaryValidator(): DisputeSummaryValidator {
    return DisputeSummaryValidator(
        mutableListOf<DisputeSummaryRule>(
            DisputeSummaryDriverIdLengthRule(32),
            DisputeSummaryCodeLengthRule(255),
            DisputeSummaryCapacityRangeRule(120, 120),
            DisputeSummaryDiscountRangeRule(6.9, 2418.6),
        ),
    )
}
