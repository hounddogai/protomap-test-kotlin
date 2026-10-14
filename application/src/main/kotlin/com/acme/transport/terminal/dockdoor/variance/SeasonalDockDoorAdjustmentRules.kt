package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.text.normalize

/** A rule a seasonal dock door adjustment must pass. Check returns its problems, or none. */
interface SeasonalDockDoorAdjustmentRule {
    fun check(item: SeasonalDockDoorAdjustment): MutableList<String>
}

data class SeasonalDockDoorAdjustmentRequiredLabelRule(var message: String) : SeasonalDockDoorAdjustmentRule {
    override fun check(item: SeasonalDockDoorAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorAdjustmentAllowedCountryRule(
    var allowed: MutableList<String>,
) : SeasonalDockDoorAdjustmentRule {
    override fun check(item: SeasonalDockDoorAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorAdjustmentBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalDockDoorAdjustmentRule {
    override fun check(item: SeasonalDockDoorAdjustment): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class SeasonalDockDoorAdjustmentMinimumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalDockDoorAdjustmentRule {
    override fun check(item: SeasonalDockDoorAdjustment): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class SeasonalDockDoorAdjustmentRequiredCodeRule(var message: String) : SeasonalDockDoorAdjustmentRule {
    override fun check(item: SeasonalDockDoorAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorAdjustmentLabelLengthRule(var maxLength: Int) : SeasonalDockDoorAdjustmentRule {
    override fun check(item: SeasonalDockDoorAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dock door adjustment. */
data class SeasonalDockDoorAdjustmentValidator(var rules: MutableList<SeasonalDockDoorAdjustmentRule>) {
    fun validate(item: SeasonalDockDoorAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDockDoorAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDockDoorAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDockDoorAdjustmentValidator(): SeasonalDockDoorAdjustmentValidator {
    return SeasonalDockDoorAdjustmentValidator(
        mutableListOf<SeasonalDockDoorAdjustmentRule>(
            SeasonalDockDoorAdjustmentRequiredLabelRule("seasonal dock door adjustment needs an entry"),
            SeasonalDockDoorAdjustmentAllowedCountryRule(mutableListOf<String>("capped", "rolling")),
            SeasonalDockDoorAdjustmentBatchSizeRangeRule(64, 120),
            SeasonalDockDoorAdjustmentMinimumRangeRule(7.5, 258.5),
            SeasonalDockDoorAdjustmentRequiredCodeRule("seasonal dock door adjustment needs a value"),
            SeasonalDockDoorAdjustmentLabelLengthRule(3600),
        ),
    )
}
