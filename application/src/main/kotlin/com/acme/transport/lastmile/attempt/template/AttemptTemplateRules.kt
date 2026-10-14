package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.normalize

/** A rule a attempt template must pass. Check returns its problems, or none. */
interface AttemptTemplateRule {
    fun check(item: AttemptTemplate): MutableList<String>
}

data class AttemptTemplateFuelLitersRangeRule(var min: Double, var max: Double) : AttemptTemplateRule {
    override fun check(item: AttemptTemplate): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AttemptTemplateRequiredTitleRule(var message: String) : AttemptTemplateRule {
    override fun check(item: AttemptTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptTemplateAllowedTitleRule(var allowed: MutableList<String>) : AttemptTemplateRule {
    override fun check(item: AttemptTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt template. */
data class AttemptTemplateValidator(var rules: MutableList<AttemptTemplateRule>) {
    fun validate(item: AttemptTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptTemplateValidator(): AttemptTemplateValidator {
    return AttemptTemplateValidator(
        mutableListOf<AttemptTemplateRule>(
            AttemptTemplateFuelLitersRangeRule(3.0, 4908.9),
            AttemptTemplateRequiredTitleRule("attempt template needs this field"),
            AttemptTemplateAllowedTitleRule(mutableListOf<String>("lenient")),
        ),
    )
}
