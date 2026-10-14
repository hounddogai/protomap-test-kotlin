package com.acme.transport.lastmile.drop.dropdraft

/** A rule a drop override must pass. Check returns its problems, or none. */
interface DropOverrideRule {
    fun check(item: DropOverride): MutableList<String>
}

data class DropOverrideWeightRangeRule(var min: Double, var max: Double) : DropOverrideRule {
    override fun check(item: DropOverride): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class DropOverrideRequiredCodeRule(var message: String) : DropOverrideRule {
    override fun check(item: DropOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropOverrideActiveGuardRule(var limit: Double) : DropOverrideRule {
    override fun check(item: DropOverride): MutableList<String> {
        if (item.active && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class DropOverrideBudgetRangeRule(var min: Double, var max: Double) : DropOverrideRule {
    override fun check(item: DropOverride): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropOverrideCarrierLengthRule(var maxLength: Int) : DropOverrideRule {
    override fun check(item: DropOverride): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropOverrideRequiredChecksumRule(var message: String) : DropOverrideRule {
    override fun check(item: DropOverride): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop override. */
data class DropOverrideValidator(var rules: MutableList<DropOverrideRule>) {
    fun validate(item: DropOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultDropOverrideValidator(): DropOverrideValidator {
    return DropOverrideValidator(
        mutableListOf<DropOverrideRule>(
            DropOverrideWeightRangeRule(4.8, 2657.6),
            DropOverrideRequiredCodeRule("drop override needs a value"),
            DropOverrideActiveGuardRule(5.3),
            DropOverrideBudgetRangeRule(9.1, 4612.3),
            DropOverrideCarrierLengthRule(86400),
            DropOverrideRequiredChecksumRule("drop override needs a value"),
        ),
    )
}
