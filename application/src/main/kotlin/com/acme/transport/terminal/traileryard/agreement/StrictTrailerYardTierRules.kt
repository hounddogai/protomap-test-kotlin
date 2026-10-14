package com.acme.transport.terminal.traileryard.agreement

/** A rule a strict trailer yard tier must pass. Check returns its problems, or none. */
interface StrictTrailerYardTierRule {
    fun check(item: StrictTrailerYardTier): MutableList<String>
}

data class StrictTrailerYardTierCodeLengthRule(var maxLength: Int) : StrictTrailerYardTierRule {
    override fun check(item: StrictTrailerYardTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardTierRequiredLabelRule(var message: String) : StrictTrailerYardTierRule {
    override fun check(item: StrictTrailerYardTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardTierRequiredChecksumRule(var message: String) : StrictTrailerYardTierRule {
    override fun check(item: StrictTrailerYardTier): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardTierRequiredUnitRule(var message: String) : StrictTrailerYardTierRule {
    override fun check(item: StrictTrailerYardTier): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardTierAmountRangeRule(var min: Double, var max: Double) : StrictTrailerYardTierRule {
    override fun check(item: StrictTrailerYardTier): MutableList<String> {
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

/** Runs every rule of a strict trailer yard tier. */
data class StrictTrailerYardTierValidator(var rules: MutableList<StrictTrailerYardTierRule>) {
    fun validate(item: StrictTrailerYardTier): MutableList<String> {
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

    fun requireValid(item: StrictTrailerYardTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTrailerYardTier>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTrailerYardTierValidator(): StrictTrailerYardTierValidator {
    return StrictTrailerYardTierValidator(
        mutableListOf<StrictTrailerYardTierRule>(
            StrictTrailerYardTierCodeLengthRule(1000),
            StrictTrailerYardTierRequiredLabelRule("strict trailer yard tier needs a value"),
            StrictTrailerYardTierRequiredChecksumRule("strict trailer yard tier needs a value"),
            StrictTrailerYardTierRequiredUnitRule("strict trailer yard tier needs a value"),
            StrictTrailerYardTierAmountRangeRule(5.5, 2136.8),
        ),
    )
}
