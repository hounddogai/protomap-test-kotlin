package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.text.normalize

/** A rule a scheduled camera event estimate must pass. Check returns its problems, or none. */
interface ScheduledCameraEventEstimateRule {
    fun check(item: ScheduledCameraEventEstimate): MutableList<String>
}

data class ScheduledCameraEventEstimateNameLengthRule(var maxLength: Int) : ScheduledCameraEventEstimateRule {
    override fun check(item: ScheduledCameraEventEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCameraEventEstimateAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledCameraEventEstimateRule {
    override fun check(item: ScheduledCameraEventEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCameraEventEstimateSummaryLengthRule(
    var maxLength: Int,
) : ScheduledCameraEventEstimateRule {
    override fun check(item: ScheduledCameraEventEstimate): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCameraEventEstimatePinnedGuardRule(var limit: Double) : ScheduledCameraEventEstimateRule {
    override fun check(item: ScheduledCameraEventEstimate): MutableList<String> {
        if (item.pinned && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled camera event estimate. */
data class ScheduledCameraEventEstimateValidator(var rules: MutableList<ScheduledCameraEventEstimateRule>) {
    fun validate(item: ScheduledCameraEventEstimate): MutableList<String> {
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

    fun requireValid(item: ScheduledCameraEventEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCameraEventEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCameraEventEstimateValidator(): ScheduledCameraEventEstimateValidator {
    return ScheduledCameraEventEstimateValidator(
        mutableListOf<ScheduledCameraEventEstimateRule>(
            ScheduledCameraEventEstimateNameLengthRule(32),
            ScheduledCameraEventEstimateAllowedCodeRule(mutableListOf<String>("legacy")),
            ScheduledCameraEventEstimateSummaryLengthRule(64),
            ScheduledCameraEventEstimatePinnedGuardRule(3.8),
        ),
    )
}
