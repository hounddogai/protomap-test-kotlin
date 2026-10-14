package com.acme.transport.linehaul.tender.rollup

/** A rule a tender rollup must pass. Check returns its problems, or none. */
interface TenderRollupRule {
    fun check(item: TenderRollup): MutableList<String>
}

data class TenderRollupTitleLengthRule(var maxLength: Int) : TenderRollupRule {
    override fun check(item: TenderRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderRollupWeightRangeRule(var min: Double, var max: Double) : TenderRollupRule {
    override fun check(item: TenderRollup): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderRollupRequiredLocaleRule(var message: String) : TenderRollupRule {
    override fun check(item: TenderRollup): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender rollup. */
data class TenderRollupValidator(var rules: MutableList<TenderRollupRule>) {
    fun validate(item: TenderRollup): MutableList<String> {
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

    fun requireValid(item: TenderRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderRollupValidator(): TenderRollupValidator {
    return TenderRollupValidator(
        mutableListOf<TenderRollupRule>(
            TenderRollupTitleLengthRule(1000),
            TenderRollupWeightRangeRule(1.6, 3142.5),
            TenderRollupRequiredLocaleRule("tender rollup needs a value"),
        ),
    )
}
