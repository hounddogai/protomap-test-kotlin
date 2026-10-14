package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.kit.text.normalize

/** A rule a seasonal emission factor exception must pass. Check returns its problems, or none. */
interface SeasonalEmissionFactorExceptionRule {
    fun check(item: SeasonalEmissionFactorException): MutableList<String>
}

data class SeasonalEmissionFactorExceptionTrailerIdLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorExceptionRule {
    override fun check(item: SeasonalEmissionFactorException): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorExceptionCodeLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorExceptionRule {
    override fun check(item: SeasonalEmissionFactorException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorExceptionCodePrefixRule(
    var prefix: String,
) : SeasonalEmissionFactorExceptionRule {
    override fun check(item: SeasonalEmissionFactorException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorExceptionLabelLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorExceptionRule {
    override fun check(item: SeasonalEmissionFactorException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorExceptionAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalEmissionFactorExceptionRule {
    override fun check(item: SeasonalEmissionFactorException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal emission factor exception. */
data class SeasonalEmissionFactorExceptionValidator(
    var rules: MutableList<SeasonalEmissionFactorExceptionRule>,
) {
    fun validate(item: SeasonalEmissionFactorException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalEmissionFactorException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEmissionFactorException>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEmissionFactorExceptionValidator(): SeasonalEmissionFactorExceptionValidator {
    return SeasonalEmissionFactorExceptionValidator(
        mutableListOf<SeasonalEmissionFactorExceptionRule>(
            SeasonalEmissionFactorExceptionTrailerIdLengthRule(3600),
            SeasonalEmissionFactorExceptionCodeLengthRule(1000),
            SeasonalEmissionFactorExceptionCodePrefixRule("REF"),
            SeasonalEmissionFactorExceptionLabelLengthRule(64),
            SeasonalEmissionFactorExceptionAllowedCodeRule(mutableListOf<String>("default", "capped")),
        ),
    )
}
