package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a declaration score must pass. Check returns its problems, or none. */
interface DeclarationScoreRule {
    fun check(item: DeclarationScore): MutableList<String>
}

data class DeclarationScoreTitleLengthRule(var maxLength: Int) : DeclarationScoreRule {
    override fun check(item: DeclarationScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationScoreRequiredNameRule(var message: String) : DeclarationScoreRule {
    override fun check(item: DeclarationScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationScoreAllowedNameRule(var allowed: MutableList<String>) : DeclarationScoreRule {
    override fun check(item: DeclarationScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationScoreCostRangeRule(var min: Double, var max: Double) : DeclarationScoreRule {
    override fun check(item: DeclarationScore): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration score. */
data class DeclarationScoreValidator(var rules: MutableList<DeclarationScoreRule>) {
    fun validate(item: DeclarationScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationScoreValidator(): DeclarationScoreValidator {
    return DeclarationScoreValidator(
        mutableListOf<DeclarationScoreRule>(
            DeclarationScoreTitleLengthRule(1000),
            DeclarationScoreRequiredNameRule("declaration score needs a value"),
            DeclarationScoreAllowedNameRule(mutableListOf<String>("global")),
            DeclarationScoreCostRangeRule(2.2, 1421.4),
        ),
    )
}
