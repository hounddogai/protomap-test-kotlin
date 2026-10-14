package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.kit.text.normalize

/** A rule a primary sortation snapshot must pass. Check returns its problems, or none. */
interface PrimarySortationSnapshotRule {
    fun check(item: PrimarySortationSnapshot): MutableList<String>
}

data class PrimarySortationSnapshotSummaryLengthRule(var maxLength: Int) : PrimarySortationSnapshotRule {
    override fun check(item: PrimarySortationSnapshot): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationSnapshotUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : PrimarySortationSnapshotRule {
    override fun check(item: PrimarySortationSnapshot): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class PrimarySortationSnapshotAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimarySortationSnapshotRule {
    override fun check(item: PrimarySortationSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationSnapshotRequiredSummaryRule(var message: String) : PrimarySortationSnapshotRule {
    override fun check(item: PrimarySortationSnapshot): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary sortation snapshot. */
data class PrimarySortationSnapshotValidator(var rules: MutableList<PrimarySortationSnapshotRule>) {
    fun validate(item: PrimarySortationSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimarySortationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimarySortationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimarySortationSnapshotValidator(): PrimarySortationSnapshotValidator {
    return PrimarySortationSnapshotValidator(
        mutableListOf<PrimarySortationSnapshotRule>(
            PrimarySortationSnapshotSummaryLengthRule(1000),
            PrimarySortationSnapshotUpdatedAtRangeRule(32, 3600),
            PrimarySortationSnapshotAllowedCodeRule(mutableListOf<String>()),
            PrimarySortationSnapshotRequiredSummaryRule("primary sortation snapshot needs this field"),
        ),
    )
}
