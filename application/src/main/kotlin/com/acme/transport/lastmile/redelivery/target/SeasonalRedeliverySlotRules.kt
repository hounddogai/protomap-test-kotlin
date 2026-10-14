package com.acme.transport.lastmile.redelivery.target

/** A rule a seasonal redelivery slot must pass. Check returns its problems, or none. */
interface SeasonalRedeliverySlotRule {
    fun check(item: SeasonalRedeliverySlot): MutableList<String>
}

data class SeasonalRedeliverySlotCodeLengthRule(var maxLength: Int) : SeasonalRedeliverySlotRule {
    override fun check(item: SeasonalRedeliverySlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliverySlotRequiredTitleRule(var message: String) : SeasonalRedeliverySlotRule {
    override fun check(item: SeasonalRedeliverySlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliverySlotTitleLengthRule(var maxLength: Int) : SeasonalRedeliverySlotRule {
    override fun check(item: SeasonalRedeliverySlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliverySlotMarginRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRedeliverySlotRule {
    override fun check(item: SeasonalRedeliverySlot): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal redelivery slot. */
data class SeasonalRedeliverySlotValidator(var rules: MutableList<SeasonalRedeliverySlotRule>) {
    fun validate(item: SeasonalRedeliverySlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRedeliverySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRedeliverySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRedeliverySlotValidator(): SeasonalRedeliverySlotValidator {
    return SeasonalRedeliverySlotValidator(
        mutableListOf<SeasonalRedeliverySlotRule>(
            SeasonalRedeliverySlotCodeLengthRule(120),
            SeasonalRedeliverySlotRequiredTitleRule("seasonal redelivery slot needs an entry"),
            SeasonalRedeliverySlotTitleLengthRule(64),
            SeasonalRedeliverySlotMarginRangeRule(6.1, 2751.6),
        ),
    )
}
