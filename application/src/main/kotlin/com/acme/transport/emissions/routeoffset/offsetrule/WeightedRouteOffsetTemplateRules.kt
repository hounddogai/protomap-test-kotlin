package com.acme.transport.emissions.routeoffset.offsetrule

/** A rule a weighted route offset template must pass. Check returns its problems, or none. */
interface WeightedRouteOffsetTemplateRule {
    fun check(item: WeightedRouteOffsetTemplate): MutableList<String>
}

data class WeightedRouteOffsetTemplateTitleLengthRule(var maxLength: Int) : WeightedRouteOffsetTemplateRule {
    override fun check(item: WeightedRouteOffsetTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetTemplateRequiredTitleRule(var message: String) : WeightedRouteOffsetTemplateRule {
    override fun check(item: WeightedRouteOffsetTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetTemplateBudgetRangeRule(
    var min: Double,
    var max: Double,
) : WeightedRouteOffsetTemplateRule {
    override fun check(item: WeightedRouteOffsetTemplate): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetTemplateCodePrefixRule(var prefix: String) : WeightedRouteOffsetTemplateRule {
    override fun check(item: WeightedRouteOffsetTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetTemplateRateRangeRule(
    var min: Double,
    var max: Double,
) : WeightedRouteOffsetTemplateRule {
    override fun check(item: WeightedRouteOffsetTemplate): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted route offset template. */
data class WeightedRouteOffsetTemplateValidator(var rules: MutableList<WeightedRouteOffsetTemplateRule>) {
    fun validate(item: WeightedRouteOffsetTemplate): MutableList<String> {
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

    fun requireValid(item: WeightedRouteOffsetTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRouteOffsetTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRouteOffsetTemplateValidator(): WeightedRouteOffsetTemplateValidator {
    return WeightedRouteOffsetTemplateValidator(
        mutableListOf<WeightedRouteOffsetTemplateRule>(
            WeightedRouteOffsetTemplateTitleLengthRule(120),
            WeightedRouteOffsetTemplateRequiredTitleRule("weighted route offset template needs a value"),
            WeightedRouteOffsetTemplateBudgetRangeRule(1.8, 1671.4),
            WeightedRouteOffsetTemplateCodePrefixRule("OPS"),
            WeightedRouteOffsetTemplateRateRangeRule(6.2, 593.6),
        ),
    )
}
