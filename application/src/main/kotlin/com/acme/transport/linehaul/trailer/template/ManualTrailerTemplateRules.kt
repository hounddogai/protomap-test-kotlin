package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.normalize

/** A rule a manual trailer template must pass. Check returns its problems, or none. */
interface ManualTrailerTemplateRule {
    fun check(item: ManualTrailerTemplate): MutableList<String>
}

data class ManualTrailerTemplateWeightRangeRule(var min: Double, var max: Double) : ManualTrailerTemplateRule {
    override fun check(item: ManualTrailerTemplate): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerTemplateAllowedTitleRule(var allowed: MutableList<String>) : ManualTrailerTemplateRule {
    override fun check(item: ManualTrailerTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerTemplateEndsAtRangeRule(var min: Int, var max: Int) : ManualTrailerTemplateRule {
    override fun check(item: ManualTrailerTemplate): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerTemplateTitleLengthRule(var maxLength: Int) : ManualTrailerTemplateRule {
    override fun check(item: ManualTrailerTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerTemplateWindowDaysRangeRule(var min: Int, var max: Int) : ManualTrailerTemplateRule {
    override fun check(item: ManualTrailerTemplate): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class ManualTrailerTemplateCodePrefixRule(var prefix: String) : ManualTrailerTemplateRule {
    override fun check(item: ManualTrailerTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer template. */
data class ManualTrailerTemplateValidator(var rules: MutableList<ManualTrailerTemplateRule>) {
    fun validate(item: ManualTrailerTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTrailerTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerTemplateValidator(): ManualTrailerTemplateValidator {
    return ManualTrailerTemplateValidator(
        mutableListOf<ManualTrailerTemplateRule>(
            ManualTrailerTemplateWeightRangeRule(7.3, 204.8),
            ManualTrailerTemplateAllowedTitleRule(mutableListOf<String>()),
            ManualTrailerTemplateEndsAtRangeRule(120, 3600),
            ManualTrailerTemplateTitleLengthRule(120),
            ManualTrailerTemplateWindowDaysRangeRule(255, 3600),
            ManualTrailerTemplateCodePrefixRule("X"),
        ),
    )
}
