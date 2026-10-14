package com.acme.transport.emissions.evcharge.snapshot

/** A rule a regional ev charge template must pass. Check returns its problems, or none. */
interface RegionalEvChargeTemplateRule {
    fun check(item: RegionalEvChargeTemplate): MutableList<String>
}

data class RegionalEvChargeTemplateReviewerLengthRule(var maxLength: Int) : RegionalEvChargeTemplateRule {
    override fun check(item: RegionalEvChargeTemplate): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeTemplateCostRangeRule(
    var min: Double,
    var max: Double,
) : RegionalEvChargeTemplateRule {
    override fun check(item: RegionalEvChargeTemplate): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeTemplateVisibleGuardRule(var limit: Double) : RegionalEvChargeTemplateRule {
    override fun check(item: RegionalEvChargeTemplate): MutableList<String> {
        if (item.visible && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeTemplateRequiredTrailerIdRule(var message: String) : RegionalEvChargeTemplateRule {
    override fun check(item: RegionalEvChargeTemplate): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeTemplateRateRangeRule(
    var min: Double,
    var max: Double,
) : RegionalEvChargeTemplateRule {
    override fun check(item: RegionalEvChargeTemplate): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional ev charge template. */
data class RegionalEvChargeTemplateValidator(var rules: MutableList<RegionalEvChargeTemplateRule>) {
    fun validate(item: RegionalEvChargeTemplate): MutableList<String> {
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

    fun requireValid(item: RegionalEvChargeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalEvChargeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalEvChargeTemplateValidator(): RegionalEvChargeTemplateValidator {
    return RegionalEvChargeTemplateValidator(
        mutableListOf<RegionalEvChargeTemplateRule>(
            RegionalEvChargeTemplateReviewerLengthRule(32),
            RegionalEvChargeTemplateCostRangeRule(7.9, 4611.1),
            RegionalEvChargeTemplateVisibleGuardRule(7.7),
            RegionalEvChargeTemplateRequiredTrailerIdRule("regional ev charge template needs an entry"),
            RegionalEvChargeTemplateRateRangeRule(1.8, 1872.7),
        ),
    )
}
