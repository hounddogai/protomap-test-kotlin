package com.acme.transport.terminal.sortation.forecast

/** A rule a adaptive sortation template must pass. Check returns its problems, or none. */
interface AdaptiveSortationTemplateRule {
    fun check(item: AdaptiveSortationTemplate): MutableList<String>
}

data class AdaptiveSortationTemplateDiscountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveSortationTemplateRule {
    override fun check(item: AdaptiveSortationTemplate): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationTemplateCodeLengthRule(var maxLength: Int) : AdaptiveSortationTemplateRule {
    override fun check(item: AdaptiveSortationTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationTemplateTitleLengthRule(var maxLength: Int) : AdaptiveSortationTemplateRule {
    override fun check(item: AdaptiveSortationTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationTemplateCeilingRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveSortationTemplateRule {
    override fun check(item: AdaptiveSortationTemplate): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class AdaptiveSortationTemplateRequiredNotesRule(var message: String) : AdaptiveSortationTemplateRule {
    override fun check(item: AdaptiveSortationTemplate): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive sortation template. */
data class AdaptiveSortationTemplateValidator(var rules: MutableList<AdaptiveSortationTemplateRule>) {
    fun validate(item: AdaptiveSortationTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveSortationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSortationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSortationTemplateValidator(): AdaptiveSortationTemplateValidator {
    return AdaptiveSortationTemplateValidator(
        mutableListOf<AdaptiveSortationTemplateRule>(
            AdaptiveSortationTemplateDiscountRangeRule(9.9, 2680.3),
            AdaptiveSortationTemplateCodeLengthRule(16),
            AdaptiveSortationTemplateTitleLengthRule(120),
            AdaptiveSortationTemplateCeilingRangeRule(6.4, 856.3),
            AdaptiveSortationTemplateRequiredNotesRule("adaptive sortation template needs an entry"),
        ),
    )
}
