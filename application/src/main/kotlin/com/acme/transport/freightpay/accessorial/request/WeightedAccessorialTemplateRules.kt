package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.kit.text.normalize

/** A rule a weighted accessorial template must pass. Check returns its problems, or none. */
interface WeightedAccessorialTemplateRule {
    fun check(item: WeightedAccessorialTemplate): MutableList<String>
}

data class WeightedAccessorialTemplateNameLengthRule(var maxLength: Int) : WeightedAccessorialTemplateRule {
    override fun check(item: WeightedAccessorialTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialTemplateAllowedSegmentRule(
    var allowed: MutableList<String>,
) : WeightedAccessorialTemplateRule {
    override fun check(item: WeightedAccessorialTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialTemplateExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedAccessorialTemplateRule {
    override fun check(item: WeightedAccessorialTemplate): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted accessorial template. */
data class WeightedAccessorialTemplateValidator(var rules: MutableList<WeightedAccessorialTemplateRule>) {
    fun validate(item: WeightedAccessorialTemplate): MutableList<String> {
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

    fun requireValid(item: WeightedAccessorialTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAccessorialTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAccessorialTemplateValidator(): WeightedAccessorialTemplateValidator {
    return WeightedAccessorialTemplateValidator(
        mutableListOf<WeightedAccessorialTemplateRule>(
            WeightedAccessorialTemplateNameLengthRule(32),
            WeightedAccessorialTemplateAllowedSegmentRule(mutableListOf<String>("lenient", "primary")),
            WeightedAccessorialTemplateExpiresAtRangeRule(255, 255),
        ),
    )
}
