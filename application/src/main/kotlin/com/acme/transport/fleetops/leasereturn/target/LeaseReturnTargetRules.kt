package com.acme.transport.fleetops.leasereturn.target

/** A rule a lease return target must pass. Check returns its problems, or none. */
interface LeaseReturnTargetRule {
    fun check(item: LeaseReturnTarget): MutableList<String>
}

data class LeaseReturnTargetPalletsRangeRule(var min: Int, var max: Int) : LeaseReturnTargetRule {
    override fun check(item: LeaseReturnTarget): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnTargetExpiresAtRangeRule(var min: Int, var max: Int) : LeaseReturnTargetRule {
    override fun check(item: LeaseReturnTarget): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnTargetRequiredPhoneRule(var message: String) : LeaseReturnTargetRule {
    override fun check(item: LeaseReturnTarget): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnTargetEndsAtRangeRule(var min: Int, var max: Int) : LeaseReturnTargetRule {
    override fun check(item: LeaseReturnTarget): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return target. */
data class LeaseReturnTargetValidator(var rules: MutableList<LeaseReturnTargetRule>) {
    fun validate(item: LeaseReturnTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnTargetValidator(): LeaseReturnTargetValidator {
    return LeaseReturnTargetValidator(
        mutableListOf<LeaseReturnTargetRule>(
            LeaseReturnTargetPalletsRangeRule(64, 255),
            LeaseReturnTargetExpiresAtRangeRule(32, 64),
            LeaseReturnTargetRequiredPhoneRule("lease return target needs a value"),
            LeaseReturnTargetEndsAtRangeRule(16, 3600),
        ),
    )
}
