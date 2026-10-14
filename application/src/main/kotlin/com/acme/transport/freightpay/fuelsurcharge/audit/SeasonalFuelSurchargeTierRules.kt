package com.acme.transport.freightpay.fuelsurcharge.audit

/** A rule a seasonal fuel surcharge tier must pass. Check returns its problems, or none. */
interface SeasonalFuelSurchargeTierRule {
    fun check(item: SeasonalFuelSurchargeTier): MutableList<String>
}

data class SeasonalFuelSurchargeTierActiveGuardRule(var limit: Double) : SeasonalFuelSurchargeTierRule {
    override fun check(item: SeasonalFuelSurchargeTier): MutableList<String> {
        if (item.active && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeTierCodePrefixRule(var prefix: String) : SeasonalFuelSurchargeTierRule {
    override fun check(item: SeasonalFuelSurchargeTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeTierRatioRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalFuelSurchargeTierRule {
    override fun check(item: SeasonalFuelSurchargeTier): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class SeasonalFuelSurchargeTierFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalFuelSurchargeTierRule {
    override fun check(item: SeasonalFuelSurchargeTier): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeTierEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalFuelSurchargeTierRule {
    override fun check(item: SeasonalFuelSurchargeTier): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeTierCodeLengthRule(var maxLength: Int) : SeasonalFuelSurchargeTierRule {
    override fun check(item: SeasonalFuelSurchargeTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal fuel surcharge tier. */
data class SeasonalFuelSurchargeTierValidator(var rules: MutableList<SeasonalFuelSurchargeTierRule>) {
    fun validate(item: SeasonalFuelSurchargeTier): MutableList<String> {
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

    fun requireValid(item: SeasonalFuelSurchargeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalFuelSurchargeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalFuelSurchargeTierValidator(): SeasonalFuelSurchargeTierValidator {
    return SeasonalFuelSurchargeTierValidator(
        mutableListOf<SeasonalFuelSurchargeTierRule>(
            SeasonalFuelSurchargeTierActiveGuardRule(1.8),
            SeasonalFuelSurchargeTierCodePrefixRule("OPS"),
            SeasonalFuelSurchargeTierRatioRangeRule(0.0, 4804.6),
            SeasonalFuelSurchargeTierFloorValueRangeRule(3.3, 2854.3),
            SeasonalFuelSurchargeTierEtaMinutesRangeRule(255, 1000),
            SeasonalFuelSurchargeTierCodeLengthRule(32),
        ),
    )
}
