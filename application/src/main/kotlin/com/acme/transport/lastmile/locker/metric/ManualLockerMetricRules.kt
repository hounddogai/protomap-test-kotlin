package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.text.normalize

/** A rule a manual locker metric must pass. Check returns its problems, or none. */
interface ManualLockerMetricRule {
    fun check(item: ManualLockerMetric): MutableList<String>
}

data class ManualLockerMetricVisibleGuardRule(var limit: Int) : ManualLockerMetricRule {
    override fun check(item: ManualLockerMetric): MutableList<String> {
        if (item.visible && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ManualLockerMetricTitleLengthRule(var maxLength: Int) : ManualLockerMetricRule {
    override fun check(item: ManualLockerMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualLockerMetricAllowedCodeRule(var allowed: MutableList<String>) : ManualLockerMetricRule {
    override fun check(item: ManualLockerMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ManualLockerMetricRequiredCodeRule(var message: String) : ManualLockerMetricRule {
    override fun check(item: ManualLockerMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualLockerMetricShiftCodeLengthRule(var maxLength: Int) : ManualLockerMetricRule {
    override fun check(item: ManualLockerMetric): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual locker metric. */
data class ManualLockerMetricValidator(var rules: MutableList<ManualLockerMetricRule>) {
    fun validate(item: ManualLockerMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualLockerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualLockerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultManualLockerMetricValidator(): ManualLockerMetricValidator {
    return ManualLockerMetricValidator(
        mutableListOf<ManualLockerMetricRule>(
            ManualLockerMetricVisibleGuardRule(32),
            ManualLockerMetricTitleLengthRule(64),
            ManualLockerMetricAllowedCodeRule(mutableListOf<String>("manual", "adaptive", "global")),
            ManualLockerMetricRequiredCodeRule("manual locker metric needs this field"),
            ManualLockerMetricShiftCodeLengthRule(16),
        ),
    )
}
