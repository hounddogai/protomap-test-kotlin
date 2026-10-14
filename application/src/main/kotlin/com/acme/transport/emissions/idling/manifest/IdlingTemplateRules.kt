package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.text.normalize

/** A rule a idling template must pass. Check returns its problems, or none. */
interface IdlingTemplateRule {
    fun check(item: IdlingTemplate): MutableList<String>
}

data class IdlingTemplateDurationMinutesRangeRule(var min: Int, var max: Int) : IdlingTemplateRule {
    override fun check(item: IdlingTemplate): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingTemplateCodePrefixRule(var prefix: String) : IdlingTemplateRule {
    override fun check(item: IdlingTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IdlingTemplateAllowedLabelRule(var allowed: MutableList<String>) : IdlingTemplateRule {
    override fun check(item: IdlingTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class IdlingTemplateRequiredCodeRule(var message: String) : IdlingTemplateRule {
    override fun check(item: IdlingTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingTemplateRequiredLabelRule(var message: String) : IdlingTemplateRule {
    override fun check(item: IdlingTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingTemplateLabelLengthRule(var maxLength: Int) : IdlingTemplateRule {
    override fun check(item: IdlingTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling template. */
data class IdlingTemplateValidator(var rules: MutableList<IdlingTemplateRule>) {
    fun validate(item: IdlingTemplate): MutableList<String> {
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

    fun requireValid(item: IdlingTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingTemplateValidator(): IdlingTemplateValidator {
    return IdlingTemplateValidator(
        mutableListOf<IdlingTemplateRule>(
            IdlingTemplateDurationMinutesRangeRule(64, 80),
            IdlingTemplateCodePrefixRule("REF"),
            IdlingTemplateAllowedLabelRule(mutableListOf<String>("fallback", "legacy")),
            IdlingTemplateRequiredCodeRule("idling template needs a value"),
            IdlingTemplateRequiredLabelRule("idling template needs an entry"),
            IdlingTemplateLabelLengthRule(16),
        ),
    )
}
