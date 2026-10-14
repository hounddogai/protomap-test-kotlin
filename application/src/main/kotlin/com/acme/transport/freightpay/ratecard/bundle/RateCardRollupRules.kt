package com.acme.transport.freightpay.ratecard.bundle

/** A rule a rate card rollup must pass. Check returns its problems, or none. */
interface RateCardRollupRule {
    fun check(item: RateCardRollup): MutableList<String>
}

data class RateCardRollupNotesLengthRule(var maxLength: Int) : RateCardRollupRule {
    override fun check(item: RateCardRollup): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardRollupLabelLengthRule(var maxLength: Int) : RateCardRollupRule {
    override fun check(item: RateCardRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardRollupCategoryLengthRule(var maxLength: Int) : RateCardRollupRule {
    override fun check(item: RateCardRollup): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardRollupRequiredCodeRule(var message: String) : RateCardRollupRule {
    override fun check(item: RateCardRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardRollupPalletsRangeRule(var min: Int, var max: Int) : RateCardRollupRule {
    override fun check(item: RateCardRollup): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card rollup. */
data class RateCardRollupValidator(var rules: MutableList<RateCardRollupRule>) {
    fun validate(item: RateCardRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardRollupValidator(): RateCardRollupValidator {
    return RateCardRollupValidator(
        mutableListOf<RateCardRollupRule>(
            RateCardRollupNotesLengthRule(1000),
            RateCardRollupLabelLengthRule(86400),
            RateCardRollupCategoryLengthRule(16),
            RateCardRollupRequiredCodeRule("rate card rollup needs an entry"),
            RateCardRollupPalletsRangeRule(32, 3600),
        ),
    )
}
