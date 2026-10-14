package com.acme.transport.fleetops.leasereturn.returnpreference

/** A rule a default lease return claim must pass. Check returns its problems, or none. */
interface DefaultLeaseReturnClaimRule {
    fun check(item: DefaultLeaseReturnClaim): MutableList<String>
}

data class DefaultLeaseReturnClaimCodeLengthRule(var maxLength: Int) : DefaultLeaseReturnClaimRule {
    override fun check(item: DefaultLeaseReturnClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLeaseReturnClaimUnitLengthRule(var maxLength: Int) : DefaultLeaseReturnClaimRule {
    override fun check(item: DefaultLeaseReturnClaim): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLeaseReturnClaimRequiredTitleRule(var message: String) : DefaultLeaseReturnClaimRule {
    override fun check(item: DefaultLeaseReturnClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultLeaseReturnClaimCategoryLengthRule(var maxLength: Int) : DefaultLeaseReturnClaimRule {
    override fun check(item: DefaultLeaseReturnClaim): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default lease return claim. */
data class DefaultLeaseReturnClaimValidator(var rules: MutableList<DefaultLeaseReturnClaimRule>) {
    fun validate(item: DefaultLeaseReturnClaim): MutableList<String> {
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

    fun requireValid(item: DefaultLeaseReturnClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultLeaseReturnClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultLeaseReturnClaimValidator(): DefaultLeaseReturnClaimValidator {
    return DefaultLeaseReturnClaimValidator(
        mutableListOf<DefaultLeaseReturnClaimRule>(
            DefaultLeaseReturnClaimCodeLengthRule(3600),
            DefaultLeaseReturnClaimUnitLengthRule(86400),
            DefaultLeaseReturnClaimRequiredTitleRule("default lease return claim needs a value"),
            DefaultLeaseReturnClaimCategoryLengthRule(16),
        ),
    )
}
