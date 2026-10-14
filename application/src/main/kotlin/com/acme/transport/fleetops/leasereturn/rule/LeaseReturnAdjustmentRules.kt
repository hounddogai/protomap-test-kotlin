package com.acme.transport.fleetops.leasereturn.rule

/** A rule a lease return adjustment must pass. Check returns its problems, or none. */
interface LeaseReturnAdjustmentRule {
    fun check(item: LeaseReturnAdjustment): MutableList<String>
}

data class LeaseReturnAdjustmentRequiredNameRule(var message: String) : LeaseReturnAdjustmentRule {
    override fun check(item: LeaseReturnAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAdjustmentTrailerIdLengthRule(var maxLength: Int) : LeaseReturnAdjustmentRule {
    override fun check(item: LeaseReturnAdjustment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAdjustmentRequiredTrailerIdRule(var message: String) : LeaseReturnAdjustmentRule {
    override fun check(item: LeaseReturnAdjustment): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return adjustment. */
data class LeaseReturnAdjustmentValidator(var rules: MutableList<LeaseReturnAdjustmentRule>) {
    fun validate(item: LeaseReturnAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnAdjustmentValidator(): LeaseReturnAdjustmentValidator {
    return LeaseReturnAdjustmentValidator(
        mutableListOf<LeaseReturnAdjustmentRule>(
            LeaseReturnAdjustmentRequiredNameRule("lease return adjustment needs this field"),
            LeaseReturnAdjustmentTrailerIdLengthRule(1000),
            LeaseReturnAdjustmentRequiredTrailerIdRule("lease return adjustment needs an entry"),
        ),
    )
}
