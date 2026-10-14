package com.acme.transport.fleetops.utilization.estimate

/** A rule a utilization snapshot must pass. Check returns its problems, or none. */
interface UtilizationSnapshotRule {
    fun check(item: UtilizationSnapshot): MutableList<String>
}

data class UtilizationSnapshotRevisionRangeRule(var min: Int, var max: Int) : UtilizationSnapshotRule {
    override fun check(item: UtilizationSnapshot): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class UtilizationSnapshotRequiredCodeRule(var message: String) : UtilizationSnapshotRule {
    override fun check(item: UtilizationSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationSnapshotCodeLengthRule(var maxLength: Int) : UtilizationSnapshotRule {
    override fun check(item: UtilizationSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationSnapshotLaneLengthRule(var maxLength: Int) : UtilizationSnapshotRule {
    override fun check(item: UtilizationSnapshot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationSnapshotLoadKgRangeRule(var min: Double, var max: Double) : UtilizationSnapshotRule {
    override fun check(item: UtilizationSnapshot): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a utilization snapshot. */
data class UtilizationSnapshotValidator(var rules: MutableList<UtilizationSnapshotRule>) {
    fun validate(item: UtilizationSnapshot): MutableList<String> {
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

    fun requireValid(item: UtilizationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationSnapshotValidator(): UtilizationSnapshotValidator {
    return UtilizationSnapshotValidator(
        mutableListOf<UtilizationSnapshotRule>(
            UtilizationSnapshotRevisionRangeRule(255, 86400),
            UtilizationSnapshotRequiredCodeRule("utilization snapshot needs an entry"),
            UtilizationSnapshotCodeLengthRule(16),
            UtilizationSnapshotLaneLengthRule(1000),
            UtilizationSnapshotLoadKgRangeRule(8.3, 1116.0),
        ),
    )
}
