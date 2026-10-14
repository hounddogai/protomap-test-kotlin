package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.normalize

/** A rule a lease return agreement must pass. Check returns its problems, or none. */
interface LeaseReturnAgreementRule {
    fun check(item: LeaseReturnAgreement): MutableList<String>
}

data class LeaseReturnAgreementRouteCodeLengthRule(var maxLength: Int) : LeaseReturnAgreementRule {
    override fun check(item: LeaseReturnAgreement): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAgreementRequiredCodeRule(var message: String) : LeaseReturnAgreementRule {
    override fun check(item: LeaseReturnAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAgreementAllowedTitleRule(var allowed: MutableList<String>) : LeaseReturnAgreementRule {
    override fun check(item: LeaseReturnAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAgreementAttemptsRangeRule(var min: Int, var max: Int) : LeaseReturnAgreementRule {
    override fun check(item: LeaseReturnAgreement): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAgreementApprovedGuardRule(var limit: Int) : LeaseReturnAgreementRule {
    override fun check(item: LeaseReturnAgreement): MutableList<String> {
        if (item.approved && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return agreement. */
data class LeaseReturnAgreementValidator(var rules: MutableList<LeaseReturnAgreementRule>) {
    fun validate(item: LeaseReturnAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnAgreementValidator(): LeaseReturnAgreementValidator {
    return LeaseReturnAgreementValidator(
        mutableListOf<LeaseReturnAgreementRule>(
            LeaseReturnAgreementRouteCodeLengthRule(86400),
            LeaseReturnAgreementRequiredCodeRule("lease return agreement needs this field"),
            LeaseReturnAgreementAllowedTitleRule(mutableListOf<String>("batched")),
            LeaseReturnAgreementAttemptsRangeRule(80, 120),
            LeaseReturnAgreementApprovedGuardRule(32),
        ),
    )
}
