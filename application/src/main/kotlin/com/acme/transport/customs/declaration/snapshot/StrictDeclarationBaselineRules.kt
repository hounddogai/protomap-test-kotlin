package com.acme.transport.customs.declaration.snapshot

/** A rule a strict declaration baseline must pass. Check returns its problems, or none. */
interface StrictDeclarationBaselineRule {
    fun check(item: StrictDeclarationBaseline): MutableList<String>
}

data class StrictDeclarationBaselineRetriesRangeRule(
    var min: Int,
    var max: Int,
) : StrictDeclarationBaselineRule {
    override fun check(item: StrictDeclarationBaseline): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class StrictDeclarationBaselineDiscountRangeRule(
    var min: Double,
    var max: Double,
) : StrictDeclarationBaselineRule {
    override fun check(item: StrictDeclarationBaseline): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationBaselineChecksumLengthRule(var maxLength: Int) : StrictDeclarationBaselineRule {
    override fun check(item: StrictDeclarationBaseline): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationBaselineUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : StrictDeclarationBaselineRule {
    override fun check(item: StrictDeclarationBaseline): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class StrictDeclarationBaselineCodePrefixRule(var prefix: String) : StrictDeclarationBaselineRule {
    override fun check(item: StrictDeclarationBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationBaselineVisibleGuardRule(var limit: Double) : StrictDeclarationBaselineRule {
    override fun check(item: StrictDeclarationBaseline): MutableList<String> {
        if (item.visible && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict declaration baseline. */
data class StrictDeclarationBaselineValidator(var rules: MutableList<StrictDeclarationBaselineRule>) {
    fun validate(item: StrictDeclarationBaseline): MutableList<String> {
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

    fun requireValid(item: StrictDeclarationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDeclarationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDeclarationBaselineValidator(): StrictDeclarationBaselineValidator {
    return StrictDeclarationBaselineValidator(
        mutableListOf<StrictDeclarationBaselineRule>(
            StrictDeclarationBaselineRetriesRangeRule(255, 3600),
            StrictDeclarationBaselineDiscountRangeRule(8.8, 618.5),
            StrictDeclarationBaselineChecksumLengthRule(32),
            StrictDeclarationBaselineUpdatedAtRangeRule(32, 1000),
            StrictDeclarationBaselineCodePrefixRule("ACM"),
            StrictDeclarationBaselineVisibleGuardRule(1.9),
        ),
    )
}
