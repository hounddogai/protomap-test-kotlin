package com.acme.transport.safety.scorecard.ledger

/** A rule a scorecard ledger must pass. Check returns its problems, or none. */
interface ScorecardLedgerRule {
    fun check(item: ScorecardLedger): MutableList<String>
}

data class ScorecardLedgerCategoryLengthRule(var maxLength: Int) : ScorecardLedgerRule {
    override fun check(item: ScorecardLedger): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardLedgerLimitRangeRule(var min: Int, var max: Int) : ScorecardLedgerRule {
    override fun check(item: ScorecardLedger): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardLedgerRequiredCodeRule(var message: String) : ScorecardLedgerRule {
    override fun check(item: ScorecardLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardLedgerCodeLengthRule(var maxLength: Int) : ScorecardLedgerRule {
    override fun check(item: ScorecardLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardLedgerFloorValueRangeRule(var min: Double, var max: Double) : ScorecardLedgerRule {
    override fun check(item: ScorecardLedger): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardLedgerTitleLengthRule(var maxLength: Int) : ScorecardLedgerRule {
    override fun check(item: ScorecardLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard ledger. */
data class ScorecardLedgerValidator(var rules: MutableList<ScorecardLedgerRule>) {
    fun validate(item: ScorecardLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardLedgerValidator(): ScorecardLedgerValidator {
    return ScorecardLedgerValidator(
        mutableListOf<ScorecardLedgerRule>(
            ScorecardLedgerCategoryLengthRule(32),
            ScorecardLedgerLimitRangeRule(32, 3600),
            ScorecardLedgerRequiredCodeRule("scorecard ledger needs an entry"),
            ScorecardLedgerCodeLengthRule(64),
            ScorecardLedgerFloorValueRangeRule(6.2, 693.1),
            ScorecardLedgerTitleLengthRule(255),
        ),
    )
}
