package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.text.normalize

/** A rule a seasonal qualification mapping must pass. Check returns its problems, or none. */
interface SeasonalQualificationMappingRule {
    fun check(item: SeasonalQualificationMapping): MutableList<String>
}

data class SeasonalQualificationMappingCodePrefixRule(var prefix: String) : SeasonalQualificationMappingRule {
    override fun check(item: SeasonalQualificationMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationMappingRequiredSourceRule(
    var message: String,
) : SeasonalQualificationMappingRule {
    override fun check(item: SeasonalQualificationMapping): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationMappingRequiredCodeRule(
    var message: String,
) : SeasonalQualificationMappingRule {
    override fun check(item: SeasonalQualificationMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationMappingSourceLengthRule(var maxLength: Int) : SeasonalQualificationMappingRule {
    override fun check(item: SeasonalQualificationMapping): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationMappingAllowedTitleRule(
    var allowed: MutableList<String>,
) : SeasonalQualificationMappingRule {
    override fun check(item: SeasonalQualificationMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationMappingCodeLengthRule(var maxLength: Int) : SeasonalQualificationMappingRule {
    override fun check(item: SeasonalQualificationMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal qualification mapping. */
data class SeasonalQualificationMappingValidator(var rules: MutableList<SeasonalQualificationMappingRule>) {
    fun validate(item: SeasonalQualificationMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalQualificationMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalQualificationMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalQualificationMappingValidator(): SeasonalQualificationMappingValidator {
    return SeasonalQualificationMappingValidator(
        mutableListOf<SeasonalQualificationMappingRule>(
            SeasonalQualificationMappingCodePrefixRule("OPS"),
            SeasonalQualificationMappingRequiredSourceRule("seasonal qualification mapping needs an entry"),
            SeasonalQualificationMappingRequiredCodeRule("seasonal qualification mapping needs this field"),
            SeasonalQualificationMappingSourceLengthRule(16),
            SeasonalQualificationMappingAllowedTitleRule(mutableListOf<String>("rolling")),
            SeasonalQualificationMappingCodeLengthRule(64),
        ),
    )
}
