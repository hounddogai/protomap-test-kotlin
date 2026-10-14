package com.acme.transport.fleetops.leasereturn.proposal

/** A rule a lease return digest must pass. Check returns its problems, or none. */
interface LeaseReturnDigestRule {
    fun check(item: LeaseReturnDigest): MutableList<String>
}

data class LeaseReturnDigestRequiredNameRule(var message: String) : LeaseReturnDigestRule {
    override fun check(item: LeaseReturnDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnDigestDiscountRangeRule(var min: Double, var max: Double) : LeaseReturnDigestRule {
    override fun check(item: LeaseReturnDigest): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnDigestRequiredContactEmailRule(var message: String) : LeaseReturnDigestRule {
    override fun check(item: LeaseReturnDigest): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnDigestThresholdRangeRule(var min: Double, var max: Double) : LeaseReturnDigestRule {
    override fun check(item: LeaseReturnDigest): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lease return digest. */
data class LeaseReturnDigestValidator(var rules: MutableList<LeaseReturnDigestRule>) {
    fun validate(item: LeaseReturnDigest): MutableList<String> {
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

    fun requireValid(item: LeaseReturnDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnDigestValidator(): LeaseReturnDigestValidator {
    return LeaseReturnDigestValidator(
        mutableListOf<LeaseReturnDigestRule>(
            LeaseReturnDigestRequiredNameRule("lease return digest needs a value"),
            LeaseReturnDigestDiscountRangeRule(3.9, 977.5),
            LeaseReturnDigestRequiredContactEmailRule("lease return digest needs this field"),
            LeaseReturnDigestThresholdRangeRule(0.4, 2653.0),
        ),
    )
}
