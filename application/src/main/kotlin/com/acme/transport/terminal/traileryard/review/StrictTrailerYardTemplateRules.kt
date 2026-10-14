package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.normalize

/** A rule a strict trailer yard template must pass. Check returns its problems, or none. */
interface StrictTrailerYardTemplateRule {
    fun check(item: StrictTrailerYardTemplate): MutableList<String>
}

data class StrictTrailerYardTemplateStopCountRangeRule(
    var min: Int,
    var max: Int,
) : StrictTrailerYardTemplateRule {
    override fun check(item: StrictTrailerYardTemplate): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardTemplateTitleLengthRule(var maxLength: Int) : StrictTrailerYardTemplateRule {
    override fun check(item: StrictTrailerYardTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardTemplateAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictTrailerYardTemplateRule {
    override fun check(item: StrictTrailerYardTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardTemplateRequiredTitleRule(var message: String) : StrictTrailerYardTemplateRule {
    override fun check(item: StrictTrailerYardTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict trailer yard template. */
data class StrictTrailerYardTemplateValidator(var rules: MutableList<StrictTrailerYardTemplateRule>) {
    fun validate(item: StrictTrailerYardTemplate): MutableList<String> {
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

    fun requireValid(item: StrictTrailerYardTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTrailerYardTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTrailerYardTemplateValidator(): StrictTrailerYardTemplateValidator {
    return StrictTrailerYardTemplateValidator(
        mutableListOf<StrictTrailerYardTemplateRule>(
            StrictTrailerYardTemplateStopCountRangeRule(120, 1000),
            StrictTrailerYardTemplateTitleLengthRule(255),
            StrictTrailerYardTemplateAllowedCodeRule(mutableListOf<String>("primary", "manual")),
            StrictTrailerYardTemplateRequiredTitleRule("strict trailer yard template needs an entry"),
        ),
    )
}
