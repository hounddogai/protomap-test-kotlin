package com.acme.transport.customs.declaration.declarationforecast

/** A rule a global declaration allocation must pass. Check returns its problems, or none. */
interface GlobalDeclarationAllocationRule {
    fun check(item: GlobalDeclarationAllocation): MutableList<String>
}

data class GlobalDeclarationAllocationRateRangeRule(
    var min: Double,
    var max: Double,
) : GlobalDeclarationAllocationRule {
    override fun check(item: GlobalDeclarationAllocation): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class GlobalDeclarationAllocationCodePrefixRule(var prefix: String) : GlobalDeclarationAllocationRule {
    override fun check(item: GlobalDeclarationAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationAllocationRequiredLaneRule(var message: String) : GlobalDeclarationAllocationRule {
    override fun check(item: GlobalDeclarationAllocation): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global declaration allocation. */
data class GlobalDeclarationAllocationValidator(var rules: MutableList<GlobalDeclarationAllocationRule>) {
    fun validate(item: GlobalDeclarationAllocation): MutableList<String> {
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

    fun requireValid(item: GlobalDeclarationAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDeclarationAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDeclarationAllocationValidator(): GlobalDeclarationAllocationValidator {
    return GlobalDeclarationAllocationValidator(
        mutableListOf<GlobalDeclarationAllocationRule>(
            GlobalDeclarationAllocationRateRangeRule(3.9, 3445.1),
            GlobalDeclarationAllocationCodePrefixRule("Q"),
            GlobalDeclarationAllocationRequiredLaneRule("global declaration allocation needs this field"),
        ),
    )
}
