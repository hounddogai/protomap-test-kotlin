package com.acme.transport.emissions.fuelburn.target

/** A rule a fuel burn segment must pass. Check returns its problems, or none. */
interface FuelBurnSegmentRule {
    fun check(item: FuelBurnSegment): MutableList<String>
}

data class FuelBurnSegmentRequiredLocaleRule(var message: String) : FuelBurnSegmentRule {
    override fun check(item: FuelBurnSegment): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSegmentRequiredCodeRule(var message: String) : FuelBurnSegmentRule {
    override fun check(item: FuelBurnSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSegmentLocaleLengthRule(var maxLength: Int) : FuelBurnSegmentRule {
    override fun check(item: FuelBurnSegment): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn segment. */
data class FuelBurnSegmentValidator(var rules: MutableList<FuelBurnSegmentRule>) {
    fun validate(item: FuelBurnSegment): MutableList<String> {
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

    fun requireValid(item: FuelBurnSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnSegmentValidator(): FuelBurnSegmentValidator {
    return FuelBurnSegmentValidator(
        mutableListOf<FuelBurnSegmentRule>(
            FuelBurnSegmentRequiredLocaleRule("fuel burn segment needs this field"),
            FuelBurnSegmentRequiredCodeRule("fuel burn segment needs an entry"),
            FuelBurnSegmentLocaleLengthRule(32),
        ),
    )
}
