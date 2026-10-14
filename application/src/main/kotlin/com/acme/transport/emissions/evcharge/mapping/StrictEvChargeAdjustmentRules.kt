package com.acme.transport.emissions.evcharge.mapping

/** A rule a strict ev charge adjustment must pass. Check returns its problems, or none. */
interface StrictEvChargeAdjustmentRule {
    fun check(item: StrictEvChargeAdjustment): MutableList<String>
}

data class StrictEvChargeAdjustmentRequiredCodeRule(var message: String) : StrictEvChargeAdjustmentRule {
    override fun check(item: StrictEvChargeAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeAdjustmentAmountRangeRule(
    var min: Double,
    var max: Double,
) : StrictEvChargeAdjustmentRule {
    override fun check(item: StrictEvChargeAdjustment): MutableList<String> {
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

data class StrictEvChargeAdjustmentLabelLengthRule(var maxLength: Int) : StrictEvChargeAdjustmentRule {
    override fun check(item: StrictEvChargeAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeAdjustmentRequiredLabelRule(var message: String) : StrictEvChargeAdjustmentRule {
    override fun check(item: StrictEvChargeAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict ev charge adjustment. */
data class StrictEvChargeAdjustmentValidator(var rules: MutableList<StrictEvChargeAdjustmentRule>) {
    fun validate(item: StrictEvChargeAdjustment): MutableList<String> {
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

    fun requireValid(item: StrictEvChargeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEvChargeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEvChargeAdjustmentValidator(): StrictEvChargeAdjustmentValidator {
    return StrictEvChargeAdjustmentValidator(
        mutableListOf<StrictEvChargeAdjustmentRule>(
            StrictEvChargeAdjustmentRequiredCodeRule("strict ev charge adjustment needs an entry"),
            StrictEvChargeAdjustmentAmountRangeRule(7.0, 2229.0),
            StrictEvChargeAdjustmentLabelLengthRule(80),
            StrictEvChargeAdjustmentRequiredLabelRule("strict ev charge adjustment needs a value"),
        ),
    )
}
