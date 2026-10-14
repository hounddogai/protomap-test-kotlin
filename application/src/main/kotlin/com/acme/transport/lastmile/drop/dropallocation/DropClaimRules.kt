package com.acme.transport.lastmile.drop.dropallocation

/** A rule a drop claim must pass. Check returns its problems, or none. */
interface DropClaimRule {
    fun check(item: DropClaim): MutableList<String>
}

data class DropClaimRequiredCodeRule(var message: String) : DropClaimRule {
    override fun check(item: DropClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropClaimCostRangeRule(var min: Double, var max: Double) : DropClaimRule {
    override fun check(item: DropClaim): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class DropClaimCodePrefixRule(var prefix: String) : DropClaimRule {
    override fun check(item: DropClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop claim. */
data class DropClaimValidator(var rules: MutableList<DropClaimRule>) {
    fun validate(item: DropClaim): MutableList<String> {
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

    fun requireValid(item: DropClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDropClaimValidator(): DropClaimValidator {
    return DropClaimValidator(
        mutableListOf<DropClaimRule>(
            DropClaimRequiredCodeRule("drop claim needs an entry"),
            DropClaimCostRangeRule(4.6, 1548.2),
            DropClaimCodePrefixRule("OPS"),
        ),
    )
}
