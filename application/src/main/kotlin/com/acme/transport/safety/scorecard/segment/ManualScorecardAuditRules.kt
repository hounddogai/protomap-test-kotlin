package com.acme.transport.safety.scorecard.segment

/** A rule a manual scorecard audit must pass. Check returns its problems, or none. */
interface ManualScorecardAuditRule {
    fun check(item: ManualScorecardAudit): MutableList<String>
}

data class ManualScorecardAuditRequiredNameRule(var message: String) : ManualScorecardAuditRule {
    override fun check(item: ManualScorecardAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualScorecardAuditCurrencyLengthRule(var maxLength: Int) : ManualScorecardAuditRule {
    override fun check(item: ManualScorecardAudit): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualScorecardAuditRequiredCodeRule(var message: String) : ManualScorecardAuditRule {
    override fun check(item: ManualScorecardAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualScorecardAuditUpdatedAtRangeRule(var min: Int, var max: Int) : ManualScorecardAuditRule {
    override fun check(item: ManualScorecardAudit): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualScorecardAuditMinimumRangeRule(var min: Double, var max: Double) : ManualScorecardAuditRule {
    override fun check(item: ManualScorecardAudit): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual scorecard audit. */
data class ManualScorecardAuditValidator(var rules: MutableList<ManualScorecardAuditRule>) {
    fun validate(item: ManualScorecardAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualScorecardAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualScorecardAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultManualScorecardAuditValidator(): ManualScorecardAuditValidator {
    return ManualScorecardAuditValidator(
        mutableListOf<ManualScorecardAuditRule>(
            ManualScorecardAuditRequiredNameRule("manual scorecard audit needs an entry"),
            ManualScorecardAuditCurrencyLengthRule(64),
            ManualScorecardAuditRequiredCodeRule("manual scorecard audit needs an entry"),
            ManualScorecardAuditUpdatedAtRangeRule(86400, 86400),
            ManualScorecardAuditMinimumRangeRule(4.4, 3980.6),
        ),
    )
}
