package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.text.normalize

/** A rule a weighted parcel template must pass. Check returns its problems, or none. */
interface WeightedParcelTemplateRule {
    fun check(item: WeightedParcelTemplate): MutableList<String>
}

data class WeightedParcelTemplateLimitRangeRule(var min: Int, var max: Int) : WeightedParcelTemplateRule {
    override fun check(item: WeightedParcelTemplate): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelTemplateAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedParcelTemplateRule {
    override fun check(item: WeightedParcelTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelTemplateUpdatedAtRangeRule(var min: Int, var max: Int) : WeightedParcelTemplateRule {
    override fun check(item: WeightedParcelTemplate): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted parcel template. */
data class WeightedParcelTemplateValidator(var rules: MutableList<WeightedParcelTemplateRule>) {
    fun validate(item: WeightedParcelTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedParcelTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedParcelTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedParcelTemplateValidator(): WeightedParcelTemplateValidator {
    return WeightedParcelTemplateValidator(
        mutableListOf<WeightedParcelTemplateRule>(
            WeightedParcelTemplateLimitRangeRule(255, 3600),
            WeightedParcelTemplateAllowedCodeRule(mutableListOf<String>()),
            WeightedParcelTemplateUpdatedAtRangeRule(64, 3600),
        ),
    )
}
