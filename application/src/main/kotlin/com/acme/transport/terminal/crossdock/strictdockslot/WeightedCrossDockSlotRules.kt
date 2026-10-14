package com.acme.transport.terminal.crossdock.strictdockslot

/** A rule a weighted cross dock slot must pass. Check returns its problems, or none. */
interface WeightedCrossDockSlotRule {
    fun check(item: WeightedCrossDockSlot): MutableList<String>
}

data class WeightedCrossDockSlotSummaryLengthRule(var maxLength: Int) : WeightedCrossDockSlotRule {
    override fun check(item: WeightedCrossDockSlot): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCrossDockSlotNameLengthRule(var maxLength: Int) : WeightedCrossDockSlotRule {
    override fun check(item: WeightedCrossDockSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCrossDockSlotMarginRangeRule(var min: Double, var max: Double) : WeightedCrossDockSlotRule {
    override fun check(item: WeightedCrossDockSlot): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCrossDockSlotCodePrefixRule(var prefix: String) : WeightedCrossDockSlotRule {
    override fun check(item: WeightedCrossDockSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCrossDockSlotGraceDaysRangeRule(var min: Int, var max: Int) : WeightedCrossDockSlotRule {
    override fun check(item: WeightedCrossDockSlot): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted cross dock slot. */
data class WeightedCrossDockSlotValidator(var rules: MutableList<WeightedCrossDockSlotRule>) {
    fun validate(item: WeightedCrossDockSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCrossDockSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCrossDockSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCrossDockSlotValidator(): WeightedCrossDockSlotValidator {
    return WeightedCrossDockSlotValidator(
        mutableListOf<WeightedCrossDockSlotRule>(
            WeightedCrossDockSlotSummaryLengthRule(80),
            WeightedCrossDockSlotNameLengthRule(32),
            WeightedCrossDockSlotMarginRangeRule(2.5, 4302.0),
            WeightedCrossDockSlotCodePrefixRule("REF"),
            WeightedCrossDockSlotGraceDaysRangeRule(255, 1000),
        ),
    )
}
