package com.acme.transport.network.servicearea.manualareathreshold

/** A rule a regional service area template must pass. Check returns its problems, or none. */
interface RegionalServiceAreaTemplateRule {
    fun check(item: RegionalServiceAreaTemplate): MutableList<String>
}

data class RegionalServiceAreaTemplateRequiredCodeRule(var message: String) : RegionalServiceAreaTemplateRule {
    override fun check(item: RegionalServiceAreaTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaTemplateDiscountRangeRule(
    var min: Double,
    var max: Double,
) : RegionalServiceAreaTemplateRule {
    override fun check(item: RegionalServiceAreaTemplate): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaTemplateRequiredLabelRule(var message: String) : RegionalServiceAreaTemplateRule {
    override fun check(item: RegionalServiceAreaTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaTemplateCountryLengthRule(var maxLength: Int) : RegionalServiceAreaTemplateRule {
    override fun check(item: RegionalServiceAreaTemplate): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional service area template. */
data class RegionalServiceAreaTemplateValidator(var rules: MutableList<RegionalServiceAreaTemplateRule>) {
    fun validate(item: RegionalServiceAreaTemplate): MutableList<String> {
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

    fun requireValid(item: RegionalServiceAreaTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalServiceAreaTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalServiceAreaTemplateValidator(): RegionalServiceAreaTemplateValidator {
    return RegionalServiceAreaTemplateValidator(
        mutableListOf<RegionalServiceAreaTemplateRule>(
            RegionalServiceAreaTemplateRequiredCodeRule("regional service area template needs this field"),
            RegionalServiceAreaTemplateDiscountRangeRule(0.5, 3750.2),
            RegionalServiceAreaTemplateRequiredLabelRule("regional service area template needs a value"),
            RegionalServiceAreaTemplateCountryLengthRule(32),
        ),
    )
}
