package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.normalize

/** A rule a scheduled defect threshold must pass. Check returns its problems, or none. */
interface ScheduledDefectThresholdRule {
    fun check(item: ScheduledDefectThreshold): MutableList<String>
}

data class ScheduledDefectThresholdRequiredNameRule(var message: String) : ScheduledDefectThresholdRule {
    override fun check(item: ScheduledDefectThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectThresholdChannelLengthRule(var maxLength: Int) : ScheduledDefectThresholdRule {
    override fun check(item: ScheduledDefectThreshold): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectThresholdRequiredChannelRule(var message: String) : ScheduledDefectThresholdRule {
    override fun check(item: ScheduledDefectThreshold): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectThresholdAllowedNameRule(
    var allowed: MutableList<String>,
) : ScheduledDefectThresholdRule {
    override fun check(item: ScheduledDefectThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled defect threshold. */
data class ScheduledDefectThresholdValidator(var rules: MutableList<ScheduledDefectThresholdRule>) {
    fun validate(item: ScheduledDefectThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDefectThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDefectThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDefectThresholdValidator(): ScheduledDefectThresholdValidator {
    return ScheduledDefectThresholdValidator(
        mutableListOf<ScheduledDefectThresholdRule>(
            ScheduledDefectThresholdRequiredNameRule("scheduled defect threshold needs a value"),
            ScheduledDefectThresholdChannelLengthRule(120),
            ScheduledDefectThresholdRequiredChannelRule("scheduled defect threshold needs an entry"),
            ScheduledDefectThresholdAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
