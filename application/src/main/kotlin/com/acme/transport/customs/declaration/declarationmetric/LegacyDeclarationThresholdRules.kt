package com.acme.transport.customs.declaration.declarationmetric

/** A rule a legacy declaration threshold must pass. Check returns its problems, or none. */
interface LegacyDeclarationThresholdRule {
    fun check(item: LegacyDeclarationThreshold): MutableList<String>
}

data class LegacyDeclarationThresholdThresholdRangeRule(
    var min: Double,
    var max: Double,
) : LegacyDeclarationThresholdRule {
    override fun check(item: LegacyDeclarationThreshold): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationThresholdCountryLengthRule(var maxLength: Int) : LegacyDeclarationThresholdRule {
    override fun check(item: LegacyDeclarationThreshold): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationThresholdLimitRangeRule(
    var min: Int,
    var max: Int,
) : LegacyDeclarationThresholdRule {
    override fun check(item: LegacyDeclarationThreshold): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LegacyDeclarationThresholdUnitLengthRule(var maxLength: Int) : LegacyDeclarationThresholdRule {
    override fun check(item: LegacyDeclarationThreshold): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationThresholdCodePrefixRule(var prefix: String) : LegacyDeclarationThresholdRule {
    override fun check(item: LegacyDeclarationThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy declaration threshold. */
data class LegacyDeclarationThresholdValidator(var rules: MutableList<LegacyDeclarationThresholdRule>) {
    fun validate(item: LegacyDeclarationThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDeclarationThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDeclarationThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDeclarationThresholdValidator(): LegacyDeclarationThresholdValidator {
    return LegacyDeclarationThresholdValidator(
        mutableListOf<LegacyDeclarationThresholdRule>(
            LegacyDeclarationThresholdThresholdRangeRule(1.7, 2687.6),
            LegacyDeclarationThresholdCountryLengthRule(80),
            LegacyDeclarationThresholdLimitRangeRule(32, 86400),
            LegacyDeclarationThresholdUnitLengthRule(255),
            LegacyDeclarationThresholdCodePrefixRule("X"),
        ),
    )
}
