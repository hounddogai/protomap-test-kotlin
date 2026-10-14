package com.acme.transport.linehaul.laneplan.planbudget

/** A rule a adaptive lane plan template must pass. Check returns its problems, or none. */
interface AdaptiveLanePlanTemplateRule {
    fun check(item: AdaptiveLanePlanTemplate): MutableList<String>
}

data class AdaptiveLanePlanTemplateBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveLanePlanTemplateRule {
    override fun check(item: AdaptiveLanePlanTemplate): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanTemplateShiftCodeLengthRule(var maxLength: Int) : AdaptiveLanePlanTemplateRule {
    override fun check(item: AdaptiveLanePlanTemplate): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanTemplateNameLengthRule(var maxLength: Int) : AdaptiveLanePlanTemplateRule {
    override fun check(item: AdaptiveLanePlanTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanTemplateExternalRefLengthRule(var maxLength: Int) : AdaptiveLanePlanTemplateRule {
    override fun check(item: AdaptiveLanePlanTemplate): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lane plan template. */
data class AdaptiveLanePlanTemplateValidator(var rules: MutableList<AdaptiveLanePlanTemplateRule>) {
    fun validate(item: AdaptiveLanePlanTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveLanePlanTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLanePlanTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLanePlanTemplateValidator(): AdaptiveLanePlanTemplateValidator {
    return AdaptiveLanePlanTemplateValidator(
        mutableListOf<AdaptiveLanePlanTemplateRule>(
            AdaptiveLanePlanTemplateBatchSizeRangeRule(32, 80),
            AdaptiveLanePlanTemplateShiftCodeLengthRule(86400),
            AdaptiveLanePlanTemplateNameLengthRule(255),
            AdaptiveLanePlanTemplateExternalRefLengthRule(255),
        ),
    )
}
