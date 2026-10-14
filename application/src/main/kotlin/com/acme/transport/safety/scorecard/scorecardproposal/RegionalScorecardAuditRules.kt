package com.acme.transport.safety.scorecard.scorecardproposal

/** A rule a regional scorecard audit must pass. Check returns its problems, or none. */
interface RegionalScorecardAuditRule {
    fun check(item: RegionalScorecardAudit): MutableList<String>
}

data class RegionalScorecardAuditCodeLengthRule(var maxLength: Int) : RegionalScorecardAuditRule {
    override fun check(item: RegionalScorecardAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalScorecardAuditRequiredCodeRule(var message: String) : RegionalScorecardAuditRule {
    override fun check(item: RegionalScorecardAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalScorecardAuditPalletsRangeRule(var min: Int, var max: Int) : RegionalScorecardAuditRule {
    override fun check(item: RegionalScorecardAudit): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class RegionalScorecardAuditCodePrefixRule(var prefix: String) : RegionalScorecardAuditRule {
    override fun check(item: RegionalScorecardAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional scorecard audit. */
data class RegionalScorecardAuditValidator(var rules: MutableList<RegionalScorecardAuditRule>) {
    fun validate(item: RegionalScorecardAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalScorecardAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalScorecardAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalScorecardAuditValidator(): RegionalScorecardAuditValidator {
    return RegionalScorecardAuditValidator(
        mutableListOf<RegionalScorecardAuditRule>(
            RegionalScorecardAuditCodeLengthRule(32),
            RegionalScorecardAuditRequiredCodeRule("regional scorecard audit needs this field"),
            RegionalScorecardAuditPalletsRangeRule(64, 3600),
            RegionalScorecardAuditCodePrefixRule("OPS"),
        ),
    )
}
