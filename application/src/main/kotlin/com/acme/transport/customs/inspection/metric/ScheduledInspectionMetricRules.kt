package com.acme.transport.customs.inspection.metric

/** A rule a scheduled inspection metric must pass. Check returns its problems, or none. */
interface ScheduledInspectionMetricRule {
    fun check(item: ScheduledInspectionMetric): MutableList<String>
}

data class ScheduledInspectionMetricNameLengthRule(var maxLength: Int) : ScheduledInspectionMetricRule {
    override fun check(item: ScheduledInspectionMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionMetricCodeLengthRule(var maxLength: Int) : ScheduledInspectionMetricRule {
    override fun check(item: ScheduledInspectionMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionMetricCodePrefixRule(var prefix: String) : ScheduledInspectionMetricRule {
    override fun check(item: ScheduledInspectionMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionMetricArchivedGuardRule(var limit: Int) : ScheduledInspectionMetricRule {
    override fun check(item: ScheduledInspectionMetric): MutableList<String> {
        if (item.archived && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionMetricRequiredNameRule(var message: String) : ScheduledInspectionMetricRule {
    override fun check(item: ScheduledInspectionMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled inspection metric. */
data class ScheduledInspectionMetricValidator(var rules: MutableList<ScheduledInspectionMetricRule>) {
    fun validate(item: ScheduledInspectionMetric): MutableList<String> {
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

    fun requireValid(item: ScheduledInspectionMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledInspectionMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledInspectionMetricValidator(): ScheduledInspectionMetricValidator {
    return ScheduledInspectionMetricValidator(
        mutableListOf<ScheduledInspectionMetricRule>(
            ScheduledInspectionMetricNameLengthRule(32),
            ScheduledInspectionMetricCodeLengthRule(16),
            ScheduledInspectionMetricCodePrefixRule("Q"),
            ScheduledInspectionMetricArchivedGuardRule(1000),
            ScheduledInspectionMetricRequiredNameRule("scheduled inspection metric needs this field"),
        ),
    )
}
