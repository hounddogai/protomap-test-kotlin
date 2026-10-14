package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.kit.text.normalize

/** A rule a scheduled defect snapshot must pass. Check returns its problems, or none. */
interface ScheduledDefectSnapshotRule {
    fun check(item: ScheduledDefectSnapshot): MutableList<String>
}

data class ScheduledDefectSnapshotAllowedSourceRule(
    var allowed: MutableList<String>,
) : ScheduledDefectSnapshotRule {
    override fun check(item: ScheduledDefectSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectSnapshotCostRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledDefectSnapshotRule {
    override fun check(item: ScheduledDefectSnapshot): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class ScheduledDefectSnapshotCodeLengthRule(var maxLength: Int) : ScheduledDefectSnapshotRule {
    override fun check(item: ScheduledDefectSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectSnapshotCodePrefixRule(var prefix: String) : ScheduledDefectSnapshotRule {
    override fun check(item: ScheduledDefectSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectSnapshotLabelLengthRule(var maxLength: Int) : ScheduledDefectSnapshotRule {
    override fun check(item: ScheduledDefectSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled defect snapshot. */
data class ScheduledDefectSnapshotValidator(var rules: MutableList<ScheduledDefectSnapshotRule>) {
    fun validate(item: ScheduledDefectSnapshot): MutableList<String> {
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

    fun requireValid(item: ScheduledDefectSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDefectSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDefectSnapshotValidator(): ScheduledDefectSnapshotValidator {
    return ScheduledDefectSnapshotValidator(
        mutableListOf<ScheduledDefectSnapshotRule>(
            ScheduledDefectSnapshotAllowedSourceRule(mutableListOf<String>()),
            ScheduledDefectSnapshotCostRangeRule(7.6, 1096.6),
            ScheduledDefectSnapshotCodeLengthRule(80),
            ScheduledDefectSnapshotCodePrefixRule("X"),
            ScheduledDefectSnapshotLabelLengthRule(80),
        ),
    )
}
