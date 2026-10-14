package com.acme.transport.customs.declaration.estimate

/** A rule a seasonal declaration tier must pass. Check returns its problems, or none. */
interface SeasonalDeclarationTierRule {
    fun check(item: SeasonalDeclarationTier): MutableList<String>
}

data class SeasonalDeclarationTierSummaryLengthRule(var maxLength: Int) : SeasonalDeclarationTierRule {
    override fun check(item: SeasonalDeclarationTier): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationTierRequiredCodeRule(var message: String) : SeasonalDeclarationTierRule {
    override fun check(item: SeasonalDeclarationTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationTierCodePrefixRule(var prefix: String) : SeasonalDeclarationTierRule {
    override fun check(item: SeasonalDeclarationTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationTierSequenceRangeRule(var min: Int, var max: Int) : SeasonalDeclarationTierRule {
    override fun check(item: SeasonalDeclarationTier): MutableList<String> {
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

data class SeasonalDeclarationTierRevisionRangeRule(var min: Int, var max: Int) : SeasonalDeclarationTierRule {
    override fun check(item: SeasonalDeclarationTier): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class SeasonalDeclarationTierTitleLengthRule(var maxLength: Int) : SeasonalDeclarationTierRule {
    override fun check(item: SeasonalDeclarationTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal declaration tier. */
data class SeasonalDeclarationTierValidator(var rules: MutableList<SeasonalDeclarationTierRule>) {
    fun validate(item: SeasonalDeclarationTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDeclarationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDeclarationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDeclarationTierValidator(): SeasonalDeclarationTierValidator {
    return SeasonalDeclarationTierValidator(
        mutableListOf<SeasonalDeclarationTierRule>(
            SeasonalDeclarationTierSummaryLengthRule(255),
            SeasonalDeclarationTierRequiredCodeRule("seasonal declaration tier needs an entry"),
            SeasonalDeclarationTierCodePrefixRule("X"),
            SeasonalDeclarationTierSequenceRangeRule(255, 1000),
            SeasonalDeclarationTierRevisionRangeRule(255, 86400),
            SeasonalDeclarationTierTitleLengthRule(80),
        ),
    )
}
