package com.acme.transport.fleetops.leasereturn.returnaudit

/** A rule a lease return summary must pass. Check returns its problems, or none. */
interface LeaseReturnSummaryRule {
    fun check(item: LeaseReturnSummary): MutableList<String>
}

data class LeaseReturnSummaryExpiresAtRangeRule(var min: Int, var max: Int) : LeaseReturnSummaryRule {
    override fun check(item: LeaseReturnSummary): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnSummaryRequiredCurrencyRule(var message: String) : LeaseReturnSummaryRule {
    override fun check(item: LeaseReturnSummary): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnSummaryRequiredRegionRule(var message: String) : LeaseReturnSummaryRule {
    override fun check(item: LeaseReturnSummary): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnSummaryRegionLengthRule(var maxLength: Int) : LeaseReturnSummaryRule {
    override fun check(item: LeaseReturnSummary): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnSummaryLabelLengthRule(var maxLength: Int) : LeaseReturnSummaryRule {
    override fun check(item: LeaseReturnSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return summary. */
data class LeaseReturnSummaryValidator(var rules: MutableList<LeaseReturnSummaryRule>) {
    fun validate(item: LeaseReturnSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnSummaryValidator(): LeaseReturnSummaryValidator {
    return LeaseReturnSummaryValidator(
        mutableListOf<LeaseReturnSummaryRule>(
            LeaseReturnSummaryExpiresAtRangeRule(16, 64),
            LeaseReturnSummaryRequiredCurrencyRule("lease return summary needs this field"),
            LeaseReturnSummaryRequiredRegionRule("lease return summary needs an entry"),
            LeaseReturnSummaryRegionLengthRule(120),
            LeaseReturnSummaryLabelLengthRule(64),
        ),
    )
}
