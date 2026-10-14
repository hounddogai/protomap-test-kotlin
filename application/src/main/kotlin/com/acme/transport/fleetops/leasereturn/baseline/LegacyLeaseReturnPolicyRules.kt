package com.acme.transport.fleetops.leasereturn.baseline

/** A rule a legacy lease return policy must pass. Check returns its problems, or none. */
interface LegacyLeaseReturnPolicyRule {
    fun check(item: LegacyLeaseReturnPolicy): MutableList<String>
}

data class LegacyLeaseReturnPolicyDescriptionLengthRule(var maxLength: Int) : LegacyLeaseReturnPolicyRule {
    override fun check(item: LegacyLeaseReturnPolicy): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnPolicyTitleLengthRule(var maxLength: Int) : LegacyLeaseReturnPolicyRule {
    override fun check(item: LegacyLeaseReturnPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnPolicyRequiredCountryRule(var message: String) : LegacyLeaseReturnPolicyRule {
    override fun check(item: LegacyLeaseReturnPolicy): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnPolicyCodeLengthRule(var maxLength: Int) : LegacyLeaseReturnPolicyRule {
    override fun check(item: LegacyLeaseReturnPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnPolicyCodePrefixRule(var prefix: String) : LegacyLeaseReturnPolicyRule {
    override fun check(item: LegacyLeaseReturnPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnPolicyRequiredCodeRule(var message: String) : LegacyLeaseReturnPolicyRule {
    override fun check(item: LegacyLeaseReturnPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lease return policy. */
data class LegacyLeaseReturnPolicyValidator(var rules: MutableList<LegacyLeaseReturnPolicyRule>) {
    fun validate(item: LegacyLeaseReturnPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyLeaseReturnPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLeaseReturnPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLeaseReturnPolicyValidator(): LegacyLeaseReturnPolicyValidator {
    return LegacyLeaseReturnPolicyValidator(
        mutableListOf<LegacyLeaseReturnPolicyRule>(
            LegacyLeaseReturnPolicyDescriptionLengthRule(32),
            LegacyLeaseReturnPolicyTitleLengthRule(64),
            LegacyLeaseReturnPolicyRequiredCountryRule("legacy lease return policy needs a value"),
            LegacyLeaseReturnPolicyCodeLengthRule(255),
            LegacyLeaseReturnPolicyCodePrefixRule("ACM"),
            LegacyLeaseReturnPolicyRequiredCodeRule("legacy lease return policy needs an entry"),
        ),
    )
}
