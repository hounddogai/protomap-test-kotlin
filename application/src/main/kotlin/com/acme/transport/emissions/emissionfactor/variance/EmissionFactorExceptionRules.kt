package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.kit.text.normalize

/** A rule a emission factor exception must pass. Check returns its problems, or none. */
interface EmissionFactorExceptionRule {
    fun check(item: EmissionFactorException): MutableList<String>
}

data class EmissionFactorExceptionCategoryLengthRule(var maxLength: Int) : EmissionFactorExceptionRule {
    override fun check(item: EmissionFactorException): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorExceptionRequiredLabelRule(var message: String) : EmissionFactorExceptionRule {
    override fun check(item: EmissionFactorException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorExceptionRequiredCodeRule(var message: String) : EmissionFactorExceptionRule {
    override fun check(item: EmissionFactorException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorExceptionCodeLengthRule(var maxLength: Int) : EmissionFactorExceptionRule {
    override fun check(item: EmissionFactorException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorExceptionLabelLengthRule(var maxLength: Int) : EmissionFactorExceptionRule {
    override fun check(item: EmissionFactorException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorExceptionAllowedCategoryRule(
    var allowed: MutableList<String>,
) : EmissionFactorExceptionRule {
    override fun check(item: EmissionFactorException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor exception. */
data class EmissionFactorExceptionValidator(var rules: MutableList<EmissionFactorExceptionRule>) {
    fun validate(item: EmissionFactorException): MutableList<String> {
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

    fun requireValid(item: EmissionFactorException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorException>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorExceptionValidator(): EmissionFactorExceptionValidator {
    return EmissionFactorExceptionValidator(
        mutableListOf<EmissionFactorExceptionRule>(
            EmissionFactorExceptionCategoryLengthRule(120),
            EmissionFactorExceptionRequiredLabelRule("emission factor exception needs this field"),
            EmissionFactorExceptionRequiredCodeRule("emission factor exception needs this field"),
            EmissionFactorExceptionCodeLengthRule(64),
            EmissionFactorExceptionLabelLengthRule(255),
            EmissionFactorExceptionAllowedCategoryRule(mutableListOf<String>("tiered", "tiered", "strict")),
        ),
    )
}
