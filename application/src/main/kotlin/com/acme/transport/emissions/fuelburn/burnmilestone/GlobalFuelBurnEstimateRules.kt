package com.acme.transport.emissions.fuelburn.burnmilestone

/** A rule a global fuel burn estimate must pass. Check returns its problems, or none. */
interface GlobalFuelBurnEstimateRule {
    fun check(item: GlobalFuelBurnEstimate): MutableList<String>
}

data class GlobalFuelBurnEstimateLabelLengthRule(var maxLength: Int) : GlobalFuelBurnEstimateRule {
    override fun check(item: GlobalFuelBurnEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnEstimateArchivedGuardRule(var limit: Double) : GlobalFuelBurnEstimateRule {
    override fun check(item: GlobalFuelBurnEstimate): MutableList<String> {
        if (item.archived && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnEstimateCeilingRangeRule(
    var min: Double,
    var max: Double,
) : GlobalFuelBurnEstimateRule {
    override fun check(item: GlobalFuelBurnEstimate): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global fuel burn estimate. */
data class GlobalFuelBurnEstimateValidator(var rules: MutableList<GlobalFuelBurnEstimateRule>) {
    fun validate(item: GlobalFuelBurnEstimate): MutableList<String> {
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

    fun requireValid(item: GlobalFuelBurnEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelBurnEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelBurnEstimateValidator(): GlobalFuelBurnEstimateValidator {
    return GlobalFuelBurnEstimateValidator(
        mutableListOf<GlobalFuelBurnEstimateRule>(
            GlobalFuelBurnEstimateLabelLengthRule(255),
            GlobalFuelBurnEstimateArchivedGuardRule(1.2),
            GlobalFuelBurnEstimateCeilingRangeRule(7.2, 2204.8),
        ),
    )
}
