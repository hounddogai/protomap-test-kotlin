package com.acme.transport.network.servicearea.areaexception

/** A rule a primary service area entry must pass. Check returns its problems, or none. */
interface PrimaryServiceAreaEntryRule {
    fun check(item: PrimaryServiceAreaEntry): MutableList<String>
}

data class PrimaryServiceAreaEntrySummaryLengthRule(var maxLength: Int) : PrimaryServiceAreaEntryRule {
    override fun check(item: PrimaryServiceAreaEntry): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaEntryFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryServiceAreaEntryRule {
    override fun check(item: PrimaryServiceAreaEntry): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaEntryAmountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryServiceAreaEntryRule {
    override fun check(item: PrimaryServiceAreaEntry): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class PrimaryServiceAreaEntryMaximumRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryServiceAreaEntryRule {
    override fun check(item: PrimaryServiceAreaEntry): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary service area entry. */
data class PrimaryServiceAreaEntryValidator(var rules: MutableList<PrimaryServiceAreaEntryRule>) {
    fun validate(item: PrimaryServiceAreaEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryServiceAreaEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryServiceAreaEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryServiceAreaEntryValidator(): PrimaryServiceAreaEntryValidator {
    return PrimaryServiceAreaEntryValidator(
        mutableListOf<PrimaryServiceAreaEntryRule>(
            PrimaryServiceAreaEntrySummaryLengthRule(1000),
            PrimaryServiceAreaEntryFuelLitersRangeRule(3.7, 2096.5),
            PrimaryServiceAreaEntryAmountRangeRule(4.9, 107.8),
            PrimaryServiceAreaEntryMaximumRangeRule(9.8, 2930.9),
        ),
    )
}
