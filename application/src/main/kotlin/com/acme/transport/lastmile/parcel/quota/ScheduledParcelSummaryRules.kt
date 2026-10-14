package com.acme.transport.lastmile.parcel.quota

/** A rule a scheduled parcel summary must pass. Check returns its problems, or none. */
interface ScheduledParcelSummaryRule {
    fun check(item: ScheduledParcelSummary): MutableList<String>
}

data class ScheduledParcelSummaryRequiredCodeRule(var message: String) : ScheduledParcelSummaryRule {
    override fun check(item: ScheduledParcelSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelSummaryLabelLengthRule(var maxLength: Int) : ScheduledParcelSummaryRule {
    override fun check(item: ScheduledParcelSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelSummarySummaryLengthRule(var maxLength: Int) : ScheduledParcelSummaryRule {
    override fun check(item: ScheduledParcelSummary): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelSummaryCodeLengthRule(var maxLength: Int) : ScheduledParcelSummaryRule {
    override fun check(item: ScheduledParcelSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelSummaryRequiredLabelRule(var message: String) : ScheduledParcelSummaryRule {
    override fun check(item: ScheduledParcelSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled parcel summary. */
data class ScheduledParcelSummaryValidator(var rules: MutableList<ScheduledParcelSummaryRule>) {
    fun validate(item: ScheduledParcelSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledParcelSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledParcelSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledParcelSummaryValidator(): ScheduledParcelSummaryValidator {
    return ScheduledParcelSummaryValidator(
        mutableListOf<ScheduledParcelSummaryRule>(
            ScheduledParcelSummaryRequiredCodeRule("scheduled parcel summary needs this field"),
            ScheduledParcelSummaryLabelLengthRule(80),
            ScheduledParcelSummarySummaryLengthRule(86400),
            ScheduledParcelSummaryCodeLengthRule(16),
            ScheduledParcelSummaryRequiredLabelRule("scheduled parcel summary needs this field"),
        ),
    )
}
