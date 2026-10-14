package com.acme.transport.fleetops.inspection.globalinspectionscore

/** A rule a scheduled inspection score must pass. Check returns its problems, or none. */
interface ScheduledInspectionScoreRule {
    fun check(item: ScheduledInspectionScore): MutableList<String>
}

data class ScheduledInspectionScoreCostRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledInspectionScoreRule {
    override fun check(item: ScheduledInspectionScore): MutableList<String> {
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

data class ScheduledInspectionScoreRequiredNameRule(var message: String) : ScheduledInspectionScoreRule {
    override fun check(item: ScheduledInspectionScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionScoreRequiredCodeRule(var message: String) : ScheduledInspectionScoreRule {
    override fun check(item: ScheduledInspectionScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionScoreChecksumLengthRule(var maxLength: Int) : ScheduledInspectionScoreRule {
    override fun check(item: ScheduledInspectionScore): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled inspection score. */
data class ScheduledInspectionScoreValidator(var rules: MutableList<ScheduledInspectionScoreRule>) {
    fun validate(item: ScheduledInspectionScore): MutableList<String> {
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

    fun requireValid(item: ScheduledInspectionScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledInspectionScore>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledInspectionScoreValidator(): ScheduledInspectionScoreValidator {
    return ScheduledInspectionScoreValidator(
        mutableListOf<ScheduledInspectionScoreRule>(
            ScheduledInspectionScoreCostRangeRule(0.6, 1638.0),
            ScheduledInspectionScoreRequiredNameRule("scheduled inspection score needs a value"),
            ScheduledInspectionScoreRequiredCodeRule("scheduled inspection score needs an entry"),
            ScheduledInspectionScoreChecksumLengthRule(120),
        ),
    )
}
