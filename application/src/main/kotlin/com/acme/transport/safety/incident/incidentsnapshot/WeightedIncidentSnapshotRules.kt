package com.acme.transport.safety.incident.incidentsnapshot

/** A rule a weighted incident snapshot must pass. Check returns its problems, or none. */
interface WeightedIncidentSnapshotRule {
    fun check(item: WeightedIncidentSnapshot): MutableList<String>
}

data class WeightedIncidentSnapshotRequiredLabelRule(var message: String) : WeightedIncidentSnapshotRule {
    override fun check(item: WeightedIncidentSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedIncidentSnapshotRequiredCodeRule(var message: String) : WeightedIncidentSnapshotRule {
    override fun check(item: WeightedIncidentSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedIncidentSnapshotLimitRangeRule(var min: Int, var max: Int) : WeightedIncidentSnapshotRule {
    override fun check(item: WeightedIncidentSnapshot): MutableList<String> {
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

data class WeightedIncidentSnapshotCodeLengthRule(var maxLength: Int) : WeightedIncidentSnapshotRule {
    override fun check(item: WeightedIncidentSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted incident snapshot. */
data class WeightedIncidentSnapshotValidator(var rules: MutableList<WeightedIncidentSnapshotRule>) {
    fun validate(item: WeightedIncidentSnapshot): MutableList<String> {
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

    fun requireValid(item: WeightedIncidentSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedIncidentSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedIncidentSnapshotValidator(): WeightedIncidentSnapshotValidator {
    return WeightedIncidentSnapshotValidator(
        mutableListOf<WeightedIncidentSnapshotRule>(
            WeightedIncidentSnapshotRequiredLabelRule("weighted incident snapshot needs an entry"),
            WeightedIncidentSnapshotRequiredCodeRule("weighted incident snapshot needs this field"),
            WeightedIncidentSnapshotLimitRangeRule(16, 86400),
            WeightedIncidentSnapshotCodeLengthRule(1000),
        ),
    )
}
