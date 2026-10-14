package com.acme.transport.crewing.restbreak.breakmapping

/** A rule a weighted rest break template must pass. Check returns its problems, or none. */
interface WeightedRestBreakTemplateRule {
    fun check(item: WeightedRestBreakTemplate): MutableList<String>
}

data class WeightedRestBreakTemplateLabelLengthRule(var maxLength: Int) : WeightedRestBreakTemplateRule {
    override fun check(item: WeightedRestBreakTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakTemplateFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : WeightedRestBreakTemplateRule {
    override fun check(item: WeightedRestBreakTemplate): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakTemplateOdometerRangeRule(
    var min: Int,
    var max: Int,
) : WeightedRestBreakTemplateRule {
    override fun check(item: WeightedRestBreakTemplate): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted rest break template. */
data class WeightedRestBreakTemplateValidator(var rules: MutableList<WeightedRestBreakTemplateRule>) {
    fun validate(item: WeightedRestBreakTemplate): MutableList<String> {
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

    fun requireValid(item: WeightedRestBreakTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRestBreakTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRestBreakTemplateValidator(): WeightedRestBreakTemplateValidator {
    return WeightedRestBreakTemplateValidator(
        mutableListOf<WeightedRestBreakTemplateRule>(
            WeightedRestBreakTemplateLabelLengthRule(255),
            WeightedRestBreakTemplateFuelLitersRangeRule(9.9, 4370.2),
            WeightedRestBreakTemplateOdometerRangeRule(86400, 86400),
        ),
    )
}
