package com.acme.transport.lastmile.drop.droprequest

/** A rule a regional drop claim must pass. Check returns its problems, or none. */
interface RegionalDropClaimRule {
    fun check(item: RegionalDropClaim): MutableList<String>
}

data class RegionalDropClaimRequiredOwnerIdRule(var message: String) : RegionalDropClaimRule {
    override fun check(item: RegionalDropClaim): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropClaimWindowDaysRangeRule(var min: Int, var max: Int) : RegionalDropClaimRule {
    override fun check(item: RegionalDropClaim): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropClaimNameLengthRule(var maxLength: Int) : RegionalDropClaimRule {
    override fun check(item: RegionalDropClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropClaimReviewerLengthRule(var maxLength: Int) : RegionalDropClaimRule {
    override fun check(item: RegionalDropClaim): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropClaimCodePrefixRule(var prefix: String) : RegionalDropClaimRule {
    override fun check(item: RegionalDropClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional drop claim. */
data class RegionalDropClaimValidator(var rules: MutableList<RegionalDropClaimRule>) {
    fun validate(item: RegionalDropClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDropClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDropClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDropClaimValidator(): RegionalDropClaimValidator {
    return RegionalDropClaimValidator(
        mutableListOf<RegionalDropClaimRule>(
            RegionalDropClaimRequiredOwnerIdRule("regional drop claim needs an entry"),
            RegionalDropClaimWindowDaysRangeRule(120, 3600),
            RegionalDropClaimNameLengthRule(1000),
            RegionalDropClaimReviewerLengthRule(32),
            RegionalDropClaimCodePrefixRule("REF"),
        ),
    )
}
