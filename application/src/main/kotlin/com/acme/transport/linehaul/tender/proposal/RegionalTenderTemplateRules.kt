package com.acme.transport.linehaul.tender.proposal

/** A rule a regional tender template must pass. Check returns its problems, or none. */
interface RegionalTenderTemplateRule {
    fun check(item: RegionalTenderTemplate): MutableList<String>
}

data class RegionalTenderTemplateFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : RegionalTenderTemplateRule {
    override fun check(item: RegionalTenderTemplate): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTenderTemplateRequiredSegmentRule(var message: String) : RegionalTenderTemplateRule {
    override fun check(item: RegionalTenderTemplate): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTenderTemplateRequiredTitleRule(var message: String) : RegionalTenderTemplateRule {
    override fun check(item: RegionalTenderTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTenderTemplateCarrierLengthRule(var maxLength: Int) : RegionalTenderTemplateRule {
    override fun check(item: RegionalTenderTemplate): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTenderTemplateBudgetRangeRule(
    var min: Double,
    var max: Double,
) : RegionalTenderTemplateRule {
    override fun check(item: RegionalTenderTemplate): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class RegionalTenderTemplateRequiredCodeRule(var message: String) : RegionalTenderTemplateRule {
    override fun check(item: RegionalTenderTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional tender template. */
data class RegionalTenderTemplateValidator(var rules: MutableList<RegionalTenderTemplateRule>) {
    fun validate(item: RegionalTenderTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTenderTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTenderTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTenderTemplateValidator(): RegionalTenderTemplateValidator {
    return RegionalTenderTemplateValidator(
        mutableListOf<RegionalTenderTemplateRule>(
            RegionalTenderTemplateFloorValueRangeRule(4.4, 1768.9),
            RegionalTenderTemplateRequiredSegmentRule("regional tender template needs this field"),
            RegionalTenderTemplateRequiredTitleRule("regional tender template needs a value"),
            RegionalTenderTemplateCarrierLengthRule(32),
            RegionalTenderTemplateBudgetRangeRule(6.9, 1636.2),
            RegionalTenderTemplateRequiredCodeRule("regional tender template needs this field"),
        ),
    )
}
