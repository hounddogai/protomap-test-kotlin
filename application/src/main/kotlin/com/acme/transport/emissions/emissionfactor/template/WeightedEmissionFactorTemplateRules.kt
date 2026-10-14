package com.acme.transport.emissions.emissionfactor.template

/** A rule a weighted emission factor template must pass. Check returns its problems, or none. */
interface WeightedEmissionFactorTemplateRule {
    fun check(item: WeightedEmissionFactorTemplate): MutableList<String>
}

data class WeightedEmissionFactorTemplateRequiredCodeRule(
    var message: String,
) : WeightedEmissionFactorTemplateRule {
    override fun check(item: WeightedEmissionFactorTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorTemplateContactEmailLengthRule(
    var maxLength: Int,
) : WeightedEmissionFactorTemplateRule {
    override fun check(item: WeightedEmissionFactorTemplate): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorTemplateCategoryLengthRule(
    var maxLength: Int,
) : WeightedEmissionFactorTemplateRule {
    override fun check(item: WeightedEmissionFactorTemplate): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorTemplatePriorityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedEmissionFactorTemplateRule {
    override fun check(item: WeightedEmissionFactorTemplate): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted emission factor template. */
data class WeightedEmissionFactorTemplateValidator(var rules: MutableList<WeightedEmissionFactorTemplateRule>) {
    fun validate(item: WeightedEmissionFactorTemplate): MutableList<String> {
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

    fun requireValid(item: WeightedEmissionFactorTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEmissionFactorTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEmissionFactorTemplateValidator(): WeightedEmissionFactorTemplateValidator {
    return WeightedEmissionFactorTemplateValidator(
        mutableListOf<WeightedEmissionFactorTemplateRule>(
            WeightedEmissionFactorTemplateRequiredCodeRule("weighted emission factor template needs a value"),
            WeightedEmissionFactorTemplateContactEmailLengthRule(32),
            WeightedEmissionFactorTemplateCategoryLengthRule(64),
            WeightedEmissionFactorTemplatePriorityRangeRule(64, 64),
        ),
    )
}
