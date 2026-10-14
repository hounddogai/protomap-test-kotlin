package com.acme.transport.safety.scorecard.defaultscorecardledger

/** A rule a scorecard adjustment must pass. Check returns its problems, or none. */
interface ScorecardAdjustmentRule {
    fun check(item: ScorecardAdjustment): MutableList<String>
}

data class ScorecardAdjustmentCodeLengthRule(var maxLength: Int) : ScorecardAdjustmentRule {
    override fun check(item: ScorecardAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAdjustmentRequiredTrailerIdRule(var message: String) : ScorecardAdjustmentRule {
    override fun check(item: ScorecardAdjustment): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAdjustmentRequiredCodeRule(var message: String) : ScorecardAdjustmentRule {
    override fun check(item: ScorecardAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAdjustmentTitleLengthRule(var maxLength: Int) : ScorecardAdjustmentRule {
    override fun check(item: ScorecardAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAdjustmentCodePrefixRule(var prefix: String) : ScorecardAdjustmentRule {
    override fun check(item: ScorecardAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAdjustmentApprovedGuardRule(var limit: Double) : ScorecardAdjustmentRule {
    override fun check(item: ScorecardAdjustment): MutableList<String> {
        if (item.approved && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard adjustment. */
data class ScorecardAdjustmentValidator(var rules: MutableList<ScorecardAdjustmentRule>) {
    fun validate(item: ScorecardAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardAdjustmentValidator(): ScorecardAdjustmentValidator {
    return ScorecardAdjustmentValidator(
        mutableListOf<ScorecardAdjustmentRule>(
            ScorecardAdjustmentCodeLengthRule(16),
            ScorecardAdjustmentRequiredTrailerIdRule("scorecard adjustment needs a value"),
            ScorecardAdjustmentRequiredCodeRule("scorecard adjustment needs an entry"),
            ScorecardAdjustmentTitleLengthRule(1000),
            ScorecardAdjustmentCodePrefixRule("X"),
            ScorecardAdjustmentApprovedGuardRule(3.0),
        ),
    )
}
