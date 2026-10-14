package com.acme.transport.customs.declaration.segment

/** A rule a legacy declaration profile must pass. Check returns its problems, or none. */
interface LegacyDeclarationProfileRule {
    fun check(item: LegacyDeclarationProfile): MutableList<String>
}

data class LegacyDeclarationProfileRequiredCodeRule(var message: String) : LegacyDeclarationProfileRule {
    override fun check(item: LegacyDeclarationProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationProfileCurrencyLengthRule(var maxLength: Int) : LegacyDeclarationProfileRule {
    override fun check(item: LegacyDeclarationProfile): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationProfileRequiredLabelRule(var message: String) : LegacyDeclarationProfileRule {
    override fun check(item: LegacyDeclarationProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationProfileLimitRangeRule(var min: Int, var max: Int) : LegacyDeclarationProfileRule {
    override fun check(item: LegacyDeclarationProfile): MutableList<String> {
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

data class LegacyDeclarationProfileLabelLengthRule(var maxLength: Int) : LegacyDeclarationProfileRule {
    override fun check(item: LegacyDeclarationProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationProfileDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : LegacyDeclarationProfileRule {
    override fun check(item: LegacyDeclarationProfile): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy declaration profile. */
data class LegacyDeclarationProfileValidator(var rules: MutableList<LegacyDeclarationProfileRule>) {
    fun validate(item: LegacyDeclarationProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDeclarationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDeclarationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDeclarationProfileValidator(): LegacyDeclarationProfileValidator {
    return LegacyDeclarationProfileValidator(
        mutableListOf<LegacyDeclarationProfileRule>(
            LegacyDeclarationProfileRequiredCodeRule("legacy declaration profile needs a value"),
            LegacyDeclarationProfileCurrencyLengthRule(86400),
            LegacyDeclarationProfileRequiredLabelRule("legacy declaration profile needs an entry"),
            LegacyDeclarationProfileLimitRangeRule(64, 86400),
            LegacyDeclarationProfileLabelLengthRule(120),
            LegacyDeclarationProfileDistanceKmRangeRule(0.0, 3535.9),
        ),
    )
}
