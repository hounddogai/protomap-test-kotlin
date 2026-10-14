package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.normalize

/** A rule a inspection template must pass. Check returns its problems, or none. */
interface InspectionTemplateRule {
    fun check(item: InspectionTemplate): MutableList<String>
}

data class InspectionTemplateCountryLengthRule(var maxLength: Int) : InspectionTemplateRule {
    override fun check(item: InspectionTemplate): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionTemplateDockLengthRule(var maxLength: Int) : InspectionTemplateRule {
    override fun check(item: InspectionTemplate): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionTemplateAllowedNameRule(var allowed: MutableList<String>) : InspectionTemplateRule {
    override fun check(item: InspectionTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection template. */
data class InspectionTemplateValidator(var rules: MutableList<InspectionTemplateRule>) {
    fun validate(item: InspectionTemplate): MutableList<String> {
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

    fun requireValid(item: InspectionTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionTemplateValidator(): InspectionTemplateValidator {
    return InspectionTemplateValidator(
        mutableListOf<InspectionTemplateRule>(
            InspectionTemplateCountryLengthRule(32),
            InspectionTemplateDockLengthRule(16),
            InspectionTemplateAllowedNameRule(mutableListOf<String>("legacy")),
        ),
    )
}
