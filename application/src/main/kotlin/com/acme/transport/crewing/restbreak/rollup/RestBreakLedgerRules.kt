package com.acme.transport.crewing.restbreak.rollup

/** A rule a rest break ledger must pass. Check returns its problems, or none. */
interface RestBreakLedgerRule {
    fun check(item: RestBreakLedger): MutableList<String>
}

data class RestBreakLedgerEndsAtRangeRule(var min: Int, var max: Int) : RestBreakLedgerRule {
    override fun check(item: RestBreakLedger): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakLedgerAmountRangeRule(var min: Double, var max: Double) : RestBreakLedgerRule {
    override fun check(item: RestBreakLedger): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakLedgerRegionLengthRule(var maxLength: Int) : RestBreakLedgerRule {
    override fun check(item: RestBreakLedger): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break ledger. */
data class RestBreakLedgerValidator(var rules: MutableList<RestBreakLedgerRule>) {
    fun validate(item: RestBreakLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakLedgerValidator(): RestBreakLedgerValidator {
    return RestBreakLedgerValidator(
        mutableListOf<RestBreakLedgerRule>(
            RestBreakLedgerEndsAtRangeRule(32, 1000),
            RestBreakLedgerAmountRangeRule(5.9, 4623.4),
            RestBreakLedgerRegionLengthRule(16),
        ),
    )
}
