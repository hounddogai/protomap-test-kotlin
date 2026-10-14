package com.acme.transport.lastmile.drop.draft

/** A rule a seasonal drop audit must pass. Check returns its problems, or none. */
interface SeasonalDropAuditRule {
    fun check(item: SeasonalDropAudit): MutableList<String>
}

data class SeasonalDropAuditStopCountRangeRule(var min: Int, var max: Int) : SeasonalDropAuditRule {
    override fun check(item: SeasonalDropAudit): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class SeasonalDropAuditRequiredLabelRule(var message: String) : SeasonalDropAuditRule {
    override fun check(item: SeasonalDropAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropAuditCodePrefixRule(var prefix: String) : SeasonalDropAuditRule {
    override fun check(item: SeasonalDropAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropAuditLabelLengthRule(var maxLength: Int) : SeasonalDropAuditRule {
    override fun check(item: SeasonalDropAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal drop audit. */
data class SeasonalDropAuditValidator(var rules: MutableList<SeasonalDropAuditRule>) {
    fun validate(item: SeasonalDropAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDropAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDropAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDropAuditValidator(): SeasonalDropAuditValidator {
    return SeasonalDropAuditValidator(
        mutableListOf<SeasonalDropAuditRule>(
            SeasonalDropAuditStopCountRangeRule(64, 3600),
            SeasonalDropAuditRequiredLabelRule("seasonal drop audit needs an entry"),
            SeasonalDropAuditCodePrefixRule("X"),
            SeasonalDropAuditLabelLengthRule(120),
        ),
    )
}
