package com.acme.transport.freightpay.freightinvoice.score

/** A rule a freight invoice milestone must pass. Check returns its problems, or none. */
interface FreightInvoiceMilestoneRule {
    fun check(item: FreightInvoiceMilestone): MutableList<String>
}

data class FreightInvoiceMilestoneLimitRangeRule(var min: Int, var max: Int) : FreightInvoiceMilestoneRule {
    override fun check(item: FreightInvoiceMilestone): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceMilestoneCodePrefixRule(var prefix: String) : FreightInvoiceMilestoneRule {
    override fun check(item: FreightInvoiceMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceMilestoneMinimumRangeRule(
    var min: Double,
    var max: Double,
) : FreightInvoiceMilestoneRule {
    override fun check(item: FreightInvoiceMilestone): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceMilestoneRequiredTimezoneRule(var message: String) : FreightInvoiceMilestoneRule {
    override fun check(item: FreightInvoiceMilestone): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceMilestoneUpdatedAtRangeRule(var min: Int, var max: Int) : FreightInvoiceMilestoneRule {
    override fun check(item: FreightInvoiceMilestone): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a freight invoice milestone. */
data class FreightInvoiceMilestoneValidator(var rules: MutableList<FreightInvoiceMilestoneRule>) {
    fun validate(item: FreightInvoiceMilestone): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceMilestoneValidator(): FreightInvoiceMilestoneValidator {
    return FreightInvoiceMilestoneValidator(
        mutableListOf<FreightInvoiceMilestoneRule>(
            FreightInvoiceMilestoneLimitRangeRule(64, 86400),
            FreightInvoiceMilestoneCodePrefixRule("ACM"),
            FreightInvoiceMilestoneMinimumRangeRule(9.7, 306.8),
            FreightInvoiceMilestoneRequiredTimezoneRule("freight invoice milestone needs an entry"),
            FreightInvoiceMilestoneUpdatedAtRangeRule(16, 120),
        ),
    )
}
