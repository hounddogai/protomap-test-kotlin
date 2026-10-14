package com.acme.transport.fleetops.defect.template

/** A rule a adaptive defect template must pass. Check returns its problems, or none. */
interface AdaptiveDefectTemplateRule {
    fun check(item: AdaptiveDefectTemplate): MutableList<String>
}

data class AdaptiveDefectTemplateSummaryLengthRule(var maxLength: Int) : AdaptiveDefectTemplateRule {
    override fun check(item: AdaptiveDefectTemplate): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectTemplateCodeLengthRule(var maxLength: Int) : AdaptiveDefectTemplateRule {
    override fun check(item: AdaptiveDefectTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectTemplateRequiredTitleRule(var message: String) : AdaptiveDefectTemplateRule {
    override fun check(item: AdaptiveDefectTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive defect template. */
data class AdaptiveDefectTemplateValidator(var rules: MutableList<AdaptiveDefectTemplateRule>) {
    fun validate(item: AdaptiveDefectTemplate): MutableList<String> {
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

    fun requireValid(item: AdaptiveDefectTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDefectTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDefectTemplateValidator(): AdaptiveDefectTemplateValidator {
    return AdaptiveDefectTemplateValidator(
        mutableListOf<AdaptiveDefectTemplateRule>(
            AdaptiveDefectTemplateSummaryLengthRule(1000),
            AdaptiveDefectTemplateCodeLengthRule(80),
            AdaptiveDefectTemplateRequiredTitleRule("adaptive defect template needs a value"),
        ),
    )
}
