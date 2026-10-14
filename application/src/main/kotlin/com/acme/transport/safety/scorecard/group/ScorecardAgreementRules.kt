package com.acme.transport.safety.scorecard.group

/** A rule a scorecard agreement must pass. Check returns its problems, or none. */
interface ScorecardAgreementRule {
    fun check(item: ScorecardAgreement): MutableList<String>
}

data class ScorecardAgreementRequiredCodeRule(var message: String) : ScorecardAgreementRule {
    override fun check(item: ScorecardAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAgreementNameLengthRule(var maxLength: Int) : ScorecardAgreementRule {
    override fun check(item: ScorecardAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAgreementCodeLengthRule(var maxLength: Int) : ScorecardAgreementRule {
    override fun check(item: ScorecardAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAgreementRequiredUnitRule(var message: String) : ScorecardAgreementRule {
    override fun check(item: ScorecardAgreement): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAgreementUnitLengthRule(var maxLength: Int) : ScorecardAgreementRule {
    override fun check(item: ScorecardAgreement): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAgreementCostRangeRule(var min: Double, var max: Double) : ScorecardAgreementRule {
    override fun check(item: ScorecardAgreement): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scorecard agreement. */
data class ScorecardAgreementValidator(var rules: MutableList<ScorecardAgreementRule>) {
    fun validate(item: ScorecardAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardAgreementValidator(): ScorecardAgreementValidator {
    return ScorecardAgreementValidator(
        mutableListOf<ScorecardAgreementRule>(
            ScorecardAgreementRequiredCodeRule("scorecard agreement needs an entry"),
            ScorecardAgreementNameLengthRule(16),
            ScorecardAgreementCodeLengthRule(64),
            ScorecardAgreementRequiredUnitRule("scorecard agreement needs a value"),
            ScorecardAgreementUnitLengthRule(32),
            ScorecardAgreementCostRangeRule(5.4, 2694.1),
        ),
    )
}
