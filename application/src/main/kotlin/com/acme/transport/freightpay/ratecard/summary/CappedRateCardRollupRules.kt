package com.acme.transport.freightpay.ratecard.summary

/** A rule a capped rate card rollup must pass. Check returns its problems, or none. */
interface CappedRateCardRollupRule {
    fun check(item: CappedRateCardRollup): MutableList<String>
}

data class CappedRateCardRollupCodePrefixRule(var prefix: String) : CappedRateCardRollupRule {
    override fun check(item: CappedRateCardRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardRollupRequiredCodeRule(var message: String) : CappedRateCardRollupRule {
    override fun check(item: CappedRateCardRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardRollupSequenceRangeRule(var min: Int, var max: Int) : CappedRateCardRollupRule {
    override fun check(item: CappedRateCardRollup): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped rate card rollup. */
data class CappedRateCardRollupValidator(var rules: MutableList<CappedRateCardRollupRule>) {
    fun validate(item: CappedRateCardRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRateCardRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRateCardRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRateCardRollupValidator(): CappedRateCardRollupValidator {
    return CappedRateCardRollupValidator(
        mutableListOf<CappedRateCardRollupRule>(
            CappedRateCardRollupCodePrefixRule("Q"),
            CappedRateCardRollupRequiredCodeRule("capped rate card rollup needs this field"),
            CappedRateCardRollupSequenceRangeRule(64, 255),
        ),
    )
}
