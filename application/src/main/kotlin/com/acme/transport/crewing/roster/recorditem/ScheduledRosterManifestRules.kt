package com.acme.transport.crewing.roster.recorditem

/** A rule a scheduled roster manifest must pass. Check returns its problems, or none. */
interface ScheduledRosterManifestRule {
    fun check(item: ScheduledRosterManifest): MutableList<String>
}

data class ScheduledRosterManifestAttemptsRangeRule(var min: Int, var max: Int) : ScheduledRosterManifestRule {
    override fun check(item: ScheduledRosterManifest): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterManifestRequiredChannelRule(var message: String) : ScheduledRosterManifestRule {
    override fun check(item: ScheduledRosterManifest): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterManifestBatchSizeRangeRule(var min: Int, var max: Int) : ScheduledRosterManifestRule {
    override fun check(item: ScheduledRosterManifest): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled roster manifest. */
data class ScheduledRosterManifestValidator(var rules: MutableList<ScheduledRosterManifestRule>) {
    fun validate(item: ScheduledRosterManifest): MutableList<String> {
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

    fun requireValid(item: ScheduledRosterManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRosterManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRosterManifestValidator(): ScheduledRosterManifestValidator {
    return ScheduledRosterManifestValidator(
        mutableListOf<ScheduledRosterManifestRule>(
            ScheduledRosterManifestAttemptsRangeRule(16, 80),
            ScheduledRosterManifestRequiredChannelRule("scheduled roster manifest needs an entry"),
            ScheduledRosterManifestBatchSizeRangeRule(32, 3600),
        ),
    )
}
