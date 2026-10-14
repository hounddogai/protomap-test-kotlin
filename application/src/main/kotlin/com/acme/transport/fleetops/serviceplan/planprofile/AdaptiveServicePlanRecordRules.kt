package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.normalize

/** A rule a adaptive service plan record must pass. Check returns its problems, or none. */
interface AdaptiveServicePlanRecordRule {
    fun check(item: AdaptiveServicePlanRecord): MutableList<String>
}

data class AdaptiveServicePlanRecordNameLengthRule(var maxLength: Int) : AdaptiveServicePlanRecordRule {
    override fun check(item: AdaptiveServicePlanRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServicePlanRecordAllowedChannelRule(
    var allowed: MutableList<String>,
) : AdaptiveServicePlanRecordRule {
    override fun check(item: AdaptiveServicePlanRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServicePlanRecordRequiredReviewerRule(var message: String) : AdaptiveServicePlanRecordRule {
    override fun check(item: AdaptiveServicePlanRecord): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive service plan record. */
data class AdaptiveServicePlanRecordValidator(var rules: MutableList<AdaptiveServicePlanRecordRule>) {
    fun validate(item: AdaptiveServicePlanRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveServicePlanRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveServicePlanRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveServicePlanRecordValidator(): AdaptiveServicePlanRecordValidator {
    return AdaptiveServicePlanRecordValidator(
        mutableListOf<AdaptiveServicePlanRecordRule>(
            AdaptiveServicePlanRecordNameLengthRule(120),
            AdaptiveServicePlanRecordAllowedChannelRule(mutableListOf<String>("manual", "default")),
            AdaptiveServicePlanRecordRequiredReviewerRule("adaptive service plan record needs this field"),
        ),
    )
}
