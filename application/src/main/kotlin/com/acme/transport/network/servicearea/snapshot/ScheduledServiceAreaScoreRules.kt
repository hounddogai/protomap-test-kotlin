package com.acme.transport.network.servicearea.snapshot

/** A rule a scheduled service area score must pass. Check returns its problems, or none. */
interface ScheduledServiceAreaScoreRule {
    fun check(item: ScheduledServiceAreaScore): MutableList<String>
}

data class ScheduledServiceAreaScoreRequiredLabelRule(var message: String) : ScheduledServiceAreaScoreRule {
    override fun check(item: ScheduledServiceAreaScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServiceAreaScorePinnedGuardRule(var limit: Int) : ScheduledServiceAreaScoreRule {
    override fun check(item: ScheduledServiceAreaScore): MutableList<String> {
        if (item.pinned && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServiceAreaScoreCodePrefixRule(var prefix: String) : ScheduledServiceAreaScoreRule {
    override fun check(item: ScheduledServiceAreaScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServiceAreaScoreCodeLengthRule(var maxLength: Int) : ScheduledServiceAreaScoreRule {
    override fun check(item: ScheduledServiceAreaScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServiceAreaScoreLabelLengthRule(var maxLength: Int) : ScheduledServiceAreaScoreRule {
    override fun check(item: ScheduledServiceAreaScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled service area score. */
data class ScheduledServiceAreaScoreValidator(var rules: MutableList<ScheduledServiceAreaScoreRule>) {
    fun validate(item: ScheduledServiceAreaScore): MutableList<String> {
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

    fun requireValid(item: ScheduledServiceAreaScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledServiceAreaScore>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledServiceAreaScoreValidator(): ScheduledServiceAreaScoreValidator {
    return ScheduledServiceAreaScoreValidator(
        mutableListOf<ScheduledServiceAreaScoreRule>(
            ScheduledServiceAreaScoreRequiredLabelRule("scheduled service area score needs an entry"),
            ScheduledServiceAreaScorePinnedGuardRule(80),
            ScheduledServiceAreaScoreCodePrefixRule("Q"),
            ScheduledServiceAreaScoreCodeLengthRule(120),
            ScheduledServiceAreaScoreLabelLengthRule(32),
        ),
    )
}
