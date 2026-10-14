package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.text.normalize

/** A rule a drop metric must pass. Check returns its problems, or none. */
interface DropMetricRule {
    fun check(item: DropMetric): MutableList<String>
}

data class DropMetricAllowedLaneRule(var allowed: MutableList<String>) : DropMetricRule {
    override fun check(item: DropMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DropMetricNotesLengthRule(var maxLength: Int) : DropMetricRule {
    override fun check(item: DropMetric): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropMetricTitleLengthRule(var maxLength: Int) : DropMetricRule {
    override fun check(item: DropMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop metric. */
data class DropMetricValidator(var rules: MutableList<DropMetricRule>) {
    fun validate(item: DropMetric): MutableList<String> {
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

    fun requireValid(item: DropMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDropMetricValidator(): DropMetricValidator {
    return DropMetricValidator(
        mutableListOf<DropMetricRule>(
            DropMetricAllowedLaneRule(mutableListOf<String>()),
            DropMetricNotesLengthRule(64),
            DropMetricTitleLengthRule(80),
        ),
    )
}
