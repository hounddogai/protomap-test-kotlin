package com.acme.transport.fleetops.leasereturn.request

/** A rule a lease return allocation must pass. Check returns its problems, or none. */
interface LeaseReturnAllocationRule {
    fun check(item: LeaseReturnAllocation): MutableList<String>
}

data class LeaseReturnAllocationCreatedAtRangeRule(var min: Int, var max: Int) : LeaseReturnAllocationRule {
    override fun check(item: LeaseReturnAllocation): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnAllocationCodeLengthRule(var maxLength: Int) : LeaseReturnAllocationRule {
    override fun check(item: LeaseReturnAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAllocationRequiredCategoryRule(var message: String) : LeaseReturnAllocationRule {
    override fun check(item: LeaseReturnAllocation): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAllocationRequiredTitleRule(var message: String) : LeaseReturnAllocationRule {
    override fun check(item: LeaseReturnAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAllocationTitleLengthRule(var maxLength: Int) : LeaseReturnAllocationRule {
    override fun check(item: LeaseReturnAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return allocation. */
data class LeaseReturnAllocationValidator(var rules: MutableList<LeaseReturnAllocationRule>) {
    fun validate(item: LeaseReturnAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnAllocationValidator(): LeaseReturnAllocationValidator {
    return LeaseReturnAllocationValidator(
        mutableListOf<LeaseReturnAllocationRule>(
            LeaseReturnAllocationCreatedAtRangeRule(86400, 86400),
            LeaseReturnAllocationCodeLengthRule(120),
            LeaseReturnAllocationRequiredCategoryRule("lease return allocation needs this field"),
            LeaseReturnAllocationRequiredTitleRule("lease return allocation needs an entry"),
            LeaseReturnAllocationTitleLengthRule(80),
        ),
    )
}
