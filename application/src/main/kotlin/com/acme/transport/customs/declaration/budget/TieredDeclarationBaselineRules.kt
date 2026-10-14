package com.acme.transport.customs.declaration.budget

/** A rule a tiered declaration baseline must pass. Check returns its problems, or none. */
interface TieredDeclarationBaselineRule {
    fun check(item: TieredDeclarationBaseline): MutableList<String>
}

data class TieredDeclarationBaselineGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : TieredDeclarationBaselineRule {
    override fun check(item: TieredDeclarationBaseline): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationBaselineRequiredLabelRule(var message: String) : TieredDeclarationBaselineRule {
    override fun check(item: TieredDeclarationBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationBaselineRequiredSummaryRule(var message: String) : TieredDeclarationBaselineRule {
    override fun check(item: TieredDeclarationBaseline): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationBaselineCodePrefixRule(var prefix: String) : TieredDeclarationBaselineRule {
    override fun check(item: TieredDeclarationBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationBaselineSummaryLengthRule(var maxLength: Int) : TieredDeclarationBaselineRule {
    override fun check(item: TieredDeclarationBaseline): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationBaselineLabelLengthRule(var maxLength: Int) : TieredDeclarationBaselineRule {
    override fun check(item: TieredDeclarationBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered declaration baseline. */
data class TieredDeclarationBaselineValidator(var rules: MutableList<TieredDeclarationBaselineRule>) {
    fun validate(item: TieredDeclarationBaseline): MutableList<String> {
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

    fun requireValid(item: TieredDeclarationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDeclarationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDeclarationBaselineValidator(): TieredDeclarationBaselineValidator {
    return TieredDeclarationBaselineValidator(
        mutableListOf<TieredDeclarationBaselineRule>(
            TieredDeclarationBaselineGraceDaysRangeRule(80, 1000),
            TieredDeclarationBaselineRequiredLabelRule("tiered declaration baseline needs a value"),
            TieredDeclarationBaselineRequiredSummaryRule("tiered declaration baseline needs an entry"),
            TieredDeclarationBaselineCodePrefixRule("REF"),
            TieredDeclarationBaselineSummaryLengthRule(16),
            TieredDeclarationBaselineLabelLengthRule(32),
        ),
    )
}
