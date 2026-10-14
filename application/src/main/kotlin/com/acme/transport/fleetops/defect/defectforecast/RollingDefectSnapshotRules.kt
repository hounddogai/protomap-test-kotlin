package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.text.normalize

/** A rule a rolling defect snapshot must pass. Check returns its problems, or none. */
interface RollingDefectSnapshotRule {
    fun check(item: RollingDefectSnapshot): MutableList<String>
}

data class RollingDefectSnapshotAttemptsRangeRule(var min: Int, var max: Int) : RollingDefectSnapshotRule {
    override fun check(item: RollingDefectSnapshot): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectSnapshotRequiredLabelRule(var message: String) : RollingDefectSnapshotRule {
    override fun check(item: RollingDefectSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectSnapshotCodeLengthRule(var maxLength: Int) : RollingDefectSnapshotRule {
    override fun check(item: RollingDefectSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectSnapshotLimitRangeRule(var min: Int, var max: Int) : RollingDefectSnapshotRule {
    override fun check(item: RollingDefectSnapshot): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class RollingDefectSnapshotAllowedLabelRule(var allowed: MutableList<String>) : RollingDefectSnapshotRule {
    override fun check(item: RollingDefectSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling defect snapshot. */
data class RollingDefectSnapshotValidator(var rules: MutableList<RollingDefectSnapshotRule>) {
    fun validate(item: RollingDefectSnapshot): MutableList<String> {
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

    fun requireValid(item: RollingDefectSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDefectSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDefectSnapshotValidator(): RollingDefectSnapshotValidator {
    return RollingDefectSnapshotValidator(
        mutableListOf<RollingDefectSnapshotRule>(
            RollingDefectSnapshotAttemptsRangeRule(32, 120),
            RollingDefectSnapshotRequiredLabelRule("rolling defect snapshot needs a value"),
            RollingDefectSnapshotCodeLengthRule(32),
            RollingDefectSnapshotLimitRangeRule(32, 255),
            RollingDefectSnapshotAllowedLabelRule(mutableListOf<String>("lenient", "rolling", "regional")),
        ),
    )
}
