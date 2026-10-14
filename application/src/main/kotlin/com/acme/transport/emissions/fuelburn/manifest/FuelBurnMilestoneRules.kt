package com.acme.transport.emissions.fuelburn.manifest

/** A rule a fuel burn milestone must pass. Check returns its problems, or none. */
interface FuelBurnMilestoneRule {
    fun check(item: FuelBurnMilestone): MutableList<String>
}

data class FuelBurnMilestoneRequiredRouteCodeRule(var message: String) : FuelBurnMilestoneRule {
    override fun check(item: FuelBurnMilestone): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnMilestoneCodePrefixRule(var prefix: String) : FuelBurnMilestoneRule {
    override fun check(item: FuelBurnMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnMilestoneTitleLengthRule(var maxLength: Int) : FuelBurnMilestoneRule {
    override fun check(item: FuelBurnMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn milestone. */
data class FuelBurnMilestoneValidator(var rules: MutableList<FuelBurnMilestoneRule>) {
    fun validate(item: FuelBurnMilestone): MutableList<String> {
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

    fun requireValid(item: FuelBurnMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnMilestoneValidator(): FuelBurnMilestoneValidator {
    return FuelBurnMilestoneValidator(
        mutableListOf<FuelBurnMilestoneRule>(
            FuelBurnMilestoneRequiredRouteCodeRule("fuel burn milestone needs this field"),
            FuelBurnMilestoneCodePrefixRule("REF"),
            FuelBurnMilestoneTitleLengthRule(16),
        ),
    )
}
