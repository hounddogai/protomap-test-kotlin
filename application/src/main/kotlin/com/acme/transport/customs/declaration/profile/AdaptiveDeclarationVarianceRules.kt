package com.acme.transport.customs.declaration.profile

/** A rule a adaptive declaration variance must pass. Check returns its problems, or none. */
interface AdaptiveDeclarationVarianceRule {
    fun check(item: AdaptiveDeclarationVariance): MutableList<String>
}

data class AdaptiveDeclarationVarianceLocaleLengthRule(var maxLength: Int) : AdaptiveDeclarationVarianceRule {
    override fun check(item: AdaptiveDeclarationVariance): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationVarianceRevisionRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveDeclarationVarianceRule {
    override fun check(item: AdaptiveDeclarationVariance): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationVarianceExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveDeclarationVarianceRule {
    override fun check(item: AdaptiveDeclarationVariance): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationVarianceNameLengthRule(var maxLength: Int) : AdaptiveDeclarationVarianceRule {
    override fun check(item: AdaptiveDeclarationVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationVarianceActiveGuardRule(var limit: Int) : AdaptiveDeclarationVarianceRule {
    override fun check(item: AdaptiveDeclarationVariance): MutableList<String> {
        if (item.active && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive declaration variance. */
data class AdaptiveDeclarationVarianceValidator(var rules: MutableList<AdaptiveDeclarationVarianceRule>) {
    fun validate(item: AdaptiveDeclarationVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDeclarationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDeclarationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDeclarationVarianceValidator(): AdaptiveDeclarationVarianceValidator {
    return AdaptiveDeclarationVarianceValidator(
        mutableListOf<AdaptiveDeclarationVarianceRule>(
            AdaptiveDeclarationVarianceLocaleLengthRule(120),
            AdaptiveDeclarationVarianceRevisionRangeRule(64, 255),
            AdaptiveDeclarationVarianceExpiresAtRangeRule(16, 120),
            AdaptiveDeclarationVarianceNameLengthRule(120),
            AdaptiveDeclarationVarianceActiveGuardRule(86400),
        ),
    )
}
