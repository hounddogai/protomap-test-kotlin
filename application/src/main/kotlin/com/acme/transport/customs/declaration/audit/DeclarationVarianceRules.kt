package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.text.normalize

/** A rule a declaration variance must pass. Check returns its problems, or none. */
interface DeclarationVarianceRule {
    fun check(item: DeclarationVariance): MutableList<String>
}

data class DeclarationVarianceSequenceRangeRule(var min: Int, var max: Int) : DeclarationVarianceRule {
    override fun check(item: DeclarationVariance): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class DeclarationVariancePhoneLengthRule(var maxLength: Int) : DeclarationVarianceRule {
    override fun check(item: DeclarationVariance): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationVarianceAllowedTitleRule(var allowed: MutableList<String>) : DeclarationVarianceRule {
    override fun check(item: DeclarationVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationVarianceRequiredCodeRule(var message: String) : DeclarationVarianceRule {
    override fun check(item: DeclarationVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration variance. */
data class DeclarationVarianceValidator(var rules: MutableList<DeclarationVarianceRule>) {
    fun validate(item: DeclarationVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationVarianceValidator(): DeclarationVarianceValidator {
    return DeclarationVarianceValidator(
        mutableListOf<DeclarationVarianceRule>(
            DeclarationVarianceSequenceRangeRule(255, 86400),
            DeclarationVariancePhoneLengthRule(64),
            DeclarationVarianceAllowedTitleRule(mutableListOf<String>()),
            DeclarationVarianceRequiredCodeRule("declaration variance needs a value"),
        ),
    )
}
