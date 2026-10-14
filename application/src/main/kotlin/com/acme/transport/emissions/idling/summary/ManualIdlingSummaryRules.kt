package com.acme.transport.emissions.idling.summary

/** A rule a manual idling summary must pass. Check returns its problems, or none. */
interface ManualIdlingSummaryRule {
    fun check(item: ManualIdlingSummary): MutableList<String>
}

data class ManualIdlingSummaryTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ManualIdlingSummaryRule {
    override fun check(item: ManualIdlingSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class ManualIdlingSummaryRequiredTitleRule(var message: String) : ManualIdlingSummaryRule {
    override fun check(item: ManualIdlingSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingSummaryTitleLengthRule(var maxLength: Int) : ManualIdlingSummaryRule {
    override fun check(item: ManualIdlingSummary): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingSummaryCodePrefixRule(var prefix: String) : ManualIdlingSummaryRule {
    override fun check(item: ManualIdlingSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual idling summary. */
data class ManualIdlingSummaryValidator(var rules: MutableList<ManualIdlingSummaryRule>) {
    fun validate(item: ManualIdlingSummary): MutableList<String> {
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

    fun requireValid(item: ManualIdlingSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIdlingSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIdlingSummaryValidator(): ManualIdlingSummaryValidator {
    return ManualIdlingSummaryValidator(
        mutableListOf<ManualIdlingSummaryRule>(
            ManualIdlingSummaryTagsLimitRule(3600, mutableListOf<String>("tiered")),
            ManualIdlingSummaryRequiredTitleRule("manual idling summary needs this field"),
            ManualIdlingSummaryTitleLengthRule(16),
            ManualIdlingSummaryCodePrefixRule("Q"),
        ),
    )
}
