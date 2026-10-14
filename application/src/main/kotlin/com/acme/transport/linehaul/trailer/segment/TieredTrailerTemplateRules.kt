package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.text.normalize

/** A rule a tiered trailer template must pass. Check returns its problems, or none. */
interface TieredTrailerTemplateRule {
    fun check(item: TieredTrailerTemplate): MutableList<String>
}

data class TieredTrailerTemplateAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : TieredTrailerTemplateRule {
    override fun check(item: TieredTrailerTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerTemplateCodeLengthRule(var maxLength: Int) : TieredTrailerTemplateRule {
    override fun check(item: TieredTrailerTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerTemplateRequiredLabelRule(var message: String) : TieredTrailerTemplateRule {
    override fun check(item: TieredTrailerTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered trailer template. */
data class TieredTrailerTemplateValidator(var rules: MutableList<TieredTrailerTemplateRule>) {
    fun validate(item: TieredTrailerTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredTrailerTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTrailerTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTrailerTemplateValidator(): TieredTrailerTemplateValidator {
    return TieredTrailerTemplateValidator(
        mutableListOf<TieredTrailerTemplateRule>(
            TieredTrailerTemplateAllowedDriverIdRule(mutableListOf<String>("regional", "primary")),
            TieredTrailerTemplateCodeLengthRule(255),
            TieredTrailerTemplateRequiredLabelRule("tiered trailer template needs this field"),
        ),
    )
}
