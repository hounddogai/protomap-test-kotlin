package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a clearance template must pass. Check returns its problems, or none. */
interface ClearanceTemplateRule {
    fun check(item: ClearanceTemplate): MutableList<String>
}

data class ClearanceTemplateRequiredCodeRule(var message: String) : ClearanceTemplateRule {
    override fun check(item: ClearanceTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceTemplateContactEmailLengthRule(var maxLength: Int) : ClearanceTemplateRule {
    override fun check(item: ClearanceTemplate): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceTemplateAllowedLocaleRule(var allowed: MutableList<String>) : ClearanceTemplateRule {
    override fun check(item: ClearanceTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceTemplateRequiredContactEmailRule(var message: String) : ClearanceTemplateRule {
    override fun check(item: ClearanceTemplate): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance template. */
data class ClearanceTemplateValidator(var rules: MutableList<ClearanceTemplateRule>) {
    fun validate(item: ClearanceTemplate): MutableList<String> {
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

    fun requireValid(item: ClearanceTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceTemplateValidator(): ClearanceTemplateValidator {
    return ClearanceTemplateValidator(
        mutableListOf<ClearanceTemplateRule>(
            ClearanceTemplateRequiredCodeRule("clearance template needs this field"),
            ClearanceTemplateContactEmailLengthRule(1000),
            ClearanceTemplateAllowedLocaleRule(mutableListOf<String>("rolling", "scheduled")),
            ClearanceTemplateRequiredContactEmailRule("clearance template needs a value"),
        ),
    )
}
