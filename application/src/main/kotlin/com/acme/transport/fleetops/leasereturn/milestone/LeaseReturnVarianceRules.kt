package com.acme.transport.fleetops.leasereturn.milestone

/** A rule a lease return variance must pass. Check returns its problems, or none. */
interface LeaseReturnVarianceRule {
    fun check(item: LeaseReturnVariance): MutableList<String>
}

data class LeaseReturnVarianceDescriptionLengthRule(var maxLength: Int) : LeaseReturnVarianceRule {
    override fun check(item: LeaseReturnVariance): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnVarianceRequiredTimezoneRule(var message: String) : LeaseReturnVarianceRule {
    override fun check(item: LeaseReturnVariance): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnVarianceCodePrefixRule(var prefix: String) : LeaseReturnVarianceRule {
    override fun check(item: LeaseReturnVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnVarianceDurationMinutesRangeRule(var min: Int, var max: Int) : LeaseReturnVarianceRule {
    override fun check(item: LeaseReturnVariance): MutableList<String> {
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

data class LeaseReturnVarianceAmountRangeRule(var min: Double, var max: Double) : LeaseReturnVarianceRule {
    override fun check(item: LeaseReturnVariance): MutableList<String> {
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

/** Runs every rule of a lease return variance. */
data class LeaseReturnVarianceValidator(var rules: MutableList<LeaseReturnVarianceRule>) {
    fun validate(item: LeaseReturnVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnVarianceValidator(): LeaseReturnVarianceValidator {
    return LeaseReturnVarianceValidator(
        mutableListOf<LeaseReturnVarianceRule>(
            LeaseReturnVarianceDescriptionLengthRule(16),
            LeaseReturnVarianceRequiredTimezoneRule("lease return variance needs this field"),
            LeaseReturnVarianceCodePrefixRule("X"),
            LeaseReturnVarianceDurationMinutesRangeRule(80, 3600),
            LeaseReturnVarianceAmountRangeRule(0.1, 4670.7),
        ),
    )
}
