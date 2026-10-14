package com.acme.transport.freightpay.audit.draft

/** A rule a seasonal audit draft must pass. Check returns its problems, or none. */
interface SeasonalAuditDraftRule {
    fun check(item: SeasonalAuditDraft): MutableList<String>
}

data class SeasonalAuditDraftCodePrefixRule(var prefix: String) : SeasonalAuditDraftRule {
    override fun check(item: SeasonalAuditDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAuditDraftDurationMinutesRangeRule(var min: Int, var max: Int) : SeasonalAuditDraftRule {
    override fun check(item: SeasonalAuditDraft): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class SeasonalAuditDraftLabelLengthRule(var maxLength: Int) : SeasonalAuditDraftRule {
    override fun check(item: SeasonalAuditDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal audit draft. */
data class SeasonalAuditDraftValidator(var rules: MutableList<SeasonalAuditDraftRule>) {
    fun validate(item: SeasonalAuditDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAuditDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAuditDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAuditDraftValidator(): SeasonalAuditDraftValidator {
    return SeasonalAuditDraftValidator(
        mutableListOf<SeasonalAuditDraftRule>(
            SeasonalAuditDraftCodePrefixRule("REF"),
            SeasonalAuditDraftDurationMinutesRangeRule(80, 255),
            SeasonalAuditDraftLabelLengthRule(80),
        ),
    )
}
