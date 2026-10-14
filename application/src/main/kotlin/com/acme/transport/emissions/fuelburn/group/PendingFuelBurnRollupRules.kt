package com.acme.transport.emissions.fuelburn.group

/** A rule a pending fuel burn rollup must pass. Check returns its problems, or none. */
interface PendingFuelBurnRollupRule {
    fun check(item: PendingFuelBurnRollup): MutableList<String>
}

data class PendingFuelBurnRollupLabelLengthRule(var maxLength: Int) : PendingFuelBurnRollupRule {
    override fun check(item: PendingFuelBurnRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnRollupCountryLengthRule(var maxLength: Int) : PendingFuelBurnRollupRule {
    override fun check(item: PendingFuelBurnRollup): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnRollupWeightRangeRule(var min: Double, var max: Double) : PendingFuelBurnRollupRule {
    override fun check(item: PendingFuelBurnRollup): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnRollupArchivedGuardRule(var limit: Double) : PendingFuelBurnRollupRule {
    override fun check(item: PendingFuelBurnRollup): MutableList<String> {
        if (item.archived && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending fuel burn rollup. */
data class PendingFuelBurnRollupValidator(var rules: MutableList<PendingFuelBurnRollupRule>) {
    fun validate(item: PendingFuelBurnRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingFuelBurnRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFuelBurnRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFuelBurnRollupValidator(): PendingFuelBurnRollupValidator {
    return PendingFuelBurnRollupValidator(
        mutableListOf<PendingFuelBurnRollupRule>(
            PendingFuelBurnRollupLabelLengthRule(16),
            PendingFuelBurnRollupCountryLengthRule(86400),
            PendingFuelBurnRollupWeightRangeRule(4.9, 4020.2),
            PendingFuelBurnRollupArchivedGuardRule(5.7),
        ),
    )
}
