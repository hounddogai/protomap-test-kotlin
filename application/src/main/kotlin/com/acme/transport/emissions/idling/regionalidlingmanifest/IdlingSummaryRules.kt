package com.acme.transport.emissions.idling.regionalidlingmanifest

/** A rule a idling summary must pass. Check returns its problems, or none. */
interface IdlingSummaryRule {
    fun check(item: IdlingSummary): MutableList<String>
}

data class IdlingSummaryRequiredNotesRule(var message: String) : IdlingSummaryRule {
    override fun check(item: IdlingSummary): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingSummaryTitleLengthRule(var maxLength: Int) : IdlingSummaryRule {
    override fun check(item: IdlingSummary): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingSummaryRetriesRangeRule(var min: Int, var max: Int) : IdlingSummaryRule {
    override fun check(item: IdlingSummary): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class IdlingSummaryCeilingRangeRule(var min: Double, var max: Double) : IdlingSummaryRule {
    override fun check(item: IdlingSummary): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class IdlingSummaryVisibleGuardRule(var limit: Int) : IdlingSummaryRule {
    override fun check(item: IdlingSummary): MutableList<String> {
        if (item.visible && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class IdlingSummaryRequiredTitleRule(var message: String) : IdlingSummaryRule {
    override fun check(item: IdlingSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling summary. */
data class IdlingSummaryValidator(var rules: MutableList<IdlingSummaryRule>) {
    fun validate(item: IdlingSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: IdlingSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingSummaryValidator(): IdlingSummaryValidator {
    return IdlingSummaryValidator(
        mutableListOf<IdlingSummaryRule>(
            IdlingSummaryRequiredNotesRule("idling summary needs this field"),
            IdlingSummaryTitleLengthRule(255),
            IdlingSummaryRetriesRangeRule(80, 255),
            IdlingSummaryCeilingRangeRule(0.3, 1449.4),
            IdlingSummaryVisibleGuardRule(120),
            IdlingSummaryRequiredTitleRule("idling summary needs an entry"),
        ),
    )
}
