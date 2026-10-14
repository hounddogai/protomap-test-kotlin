package com.acme.transport.freightpay.dispute.variance

/** A rule a global dispute profile must pass. Check returns its problems, or none. */
interface GlobalDisputeProfileRule {
    fun check(item: GlobalDisputeProfile): MutableList<String>
}

data class GlobalDisputeProfileCodeLengthRule(var maxLength: Int) : GlobalDisputeProfileRule {
    override fun check(item: GlobalDisputeProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeProfileLimitRangeRule(var min: Int, var max: Int) : GlobalDisputeProfileRule {
    override fun check(item: GlobalDisputeProfile): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class GlobalDisputeProfileExternalRefLengthRule(var maxLength: Int) : GlobalDisputeProfileRule {
    override fun check(item: GlobalDisputeProfile): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global dispute profile. */
data class GlobalDisputeProfileValidator(var rules: MutableList<GlobalDisputeProfileRule>) {
    fun validate(item: GlobalDisputeProfile): MutableList<String> {
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

    fun requireValid(item: GlobalDisputeProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDisputeProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDisputeProfileValidator(): GlobalDisputeProfileValidator {
    return GlobalDisputeProfileValidator(
        mutableListOf<GlobalDisputeProfileRule>(
            GlobalDisputeProfileCodeLengthRule(255),
            GlobalDisputeProfileLimitRangeRule(16, 80),
            GlobalDisputeProfileExternalRefLengthRule(1000),
        ),
    )
}
