package com.acme.transport.customs.declaration.digest

/** A rule a tiered declaration profile must pass. Check returns its problems, or none. */
interface TieredDeclarationProfileRule {
    fun check(item: TieredDeclarationProfile): MutableList<String>
}

data class TieredDeclarationProfileAmountRangeRule(
    var min: Double,
    var max: Double,
) : TieredDeclarationProfileRule {
    override fun check(item: TieredDeclarationProfile): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class TieredDeclarationProfileThresholdRangeRule(
    var min: Double,
    var max: Double,
) : TieredDeclarationProfileRule {
    override fun check(item: TieredDeclarationProfile): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationProfileTitleLengthRule(var maxLength: Int) : TieredDeclarationProfileRule {
    override fun check(item: TieredDeclarationProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationProfileCodeLengthRule(var maxLength: Int) : TieredDeclarationProfileRule {
    override fun check(item: TieredDeclarationProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationProfileRequiredTitleRule(var message: String) : TieredDeclarationProfileRule {
    override fun check(item: TieredDeclarationProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered declaration profile. */
data class TieredDeclarationProfileValidator(var rules: MutableList<TieredDeclarationProfileRule>) {
    fun validate(item: TieredDeclarationProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDeclarationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDeclarationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDeclarationProfileValidator(): TieredDeclarationProfileValidator {
    return TieredDeclarationProfileValidator(
        mutableListOf<TieredDeclarationProfileRule>(
            TieredDeclarationProfileAmountRangeRule(5.5, 2603.8),
            TieredDeclarationProfileThresholdRangeRule(6.9, 4738.0),
            TieredDeclarationProfileTitleLengthRule(32),
            TieredDeclarationProfileCodeLengthRule(16),
            TieredDeclarationProfileRequiredTitleRule("tiered declaration profile needs an entry"),
        ),
    )
}
