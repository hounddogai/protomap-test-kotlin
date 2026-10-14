package com.acme.transport.emissions.evcharge.adjustment

/** A rule a seasonal ev charge adjustment must pass. Check returns its problems, or none. */
interface SeasonalEvChargeAdjustmentRule {
    fun check(item: SeasonalEvChargeAdjustment): MutableList<String>
}

data class SeasonalEvChargeAdjustmentRequiredNameRule(var message: String) : SeasonalEvChargeAdjustmentRule {
    override fun check(item: SeasonalEvChargeAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeAdjustmentRequiredCodeRule(var message: String) : SeasonalEvChargeAdjustmentRule {
    override fun check(item: SeasonalEvChargeAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeAdjustmentBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalEvChargeAdjustmentRule {
    override fun check(item: SeasonalEvChargeAdjustment): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeAdjustmentReviewerLengthRule(var maxLength: Int) : SeasonalEvChargeAdjustmentRule {
    override fun check(item: SeasonalEvChargeAdjustment): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal ev charge adjustment. */
data class SeasonalEvChargeAdjustmentValidator(var rules: MutableList<SeasonalEvChargeAdjustmentRule>) {
    fun validate(item: SeasonalEvChargeAdjustment): MutableList<String> {
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

    fun requireValid(item: SeasonalEvChargeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEvChargeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEvChargeAdjustmentValidator(): SeasonalEvChargeAdjustmentValidator {
    return SeasonalEvChargeAdjustmentValidator(
        mutableListOf<SeasonalEvChargeAdjustmentRule>(
            SeasonalEvChargeAdjustmentRequiredNameRule("seasonal ev charge adjustment needs an entry"),
            SeasonalEvChargeAdjustmentRequiredCodeRule("seasonal ev charge adjustment needs this field"),
            SeasonalEvChargeAdjustmentBatchSizeRangeRule(16, 64),
            SeasonalEvChargeAdjustmentReviewerLengthRule(1000),
        ),
    )
}
