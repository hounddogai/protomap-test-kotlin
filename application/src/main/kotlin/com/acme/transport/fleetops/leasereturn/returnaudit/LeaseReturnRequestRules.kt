package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.text.normalize

/** A rule a lease return request must pass. Check returns its problems, or none. */
interface LeaseReturnRequestRule {
    fun check(item: LeaseReturnRequest): MutableList<String>
}

data class LeaseReturnRequestRequiredCodeRule(var message: String) : LeaseReturnRequestRule {
    override fun check(item: LeaseReturnRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRequestRevisionRangeRule(var min: Int, var max: Int) : LeaseReturnRequestRule {
    override fun check(item: LeaseReturnRequest): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnRequestAllowedChannelRule(var allowed: MutableList<String>) : LeaseReturnRequestRule {
    override fun check(item: LeaseReturnRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return request. */
data class LeaseReturnRequestValidator(var rules: MutableList<LeaseReturnRequestRule>) {
    fun validate(item: LeaseReturnRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnRequestValidator(): LeaseReturnRequestValidator {
    return LeaseReturnRequestValidator(
        mutableListOf<LeaseReturnRequestRule>(
            LeaseReturnRequestRequiredCodeRule("lease return request needs an entry"),
            LeaseReturnRequestRevisionRangeRule(32, 1000),
            LeaseReturnRequestAllowedChannelRule(mutableListOf<String>("batched", "legacy")),
        ),
    )
}
