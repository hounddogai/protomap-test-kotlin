package com.acme.transport.linehaul.laneplan.planbundle

/** A rule a pending lane plan checkpoint must pass. Check returns its problems, or none. */
interface PendingLanePlanCheckpointRule {
    fun check(item: PendingLanePlanCheckpoint): MutableList<String>
}

data class PendingLanePlanCheckpointCodeLengthRule(var maxLength: Int) : PendingLanePlanCheckpointRule {
    override fun check(item: PendingLanePlanCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanCheckpointVisibleGuardRule(var limit: Int) : PendingLanePlanCheckpointRule {
    override fun check(item: PendingLanePlanCheckpoint): MutableList<String> {
        if (item.visible && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanCheckpointLaneLengthRule(var maxLength: Int) : PendingLanePlanCheckpointRule {
    override fun check(item: PendingLanePlanCheckpoint): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanCheckpointRequiredCountryRule(var message: String) : PendingLanePlanCheckpointRule {
    override fun check(item: PendingLanePlanCheckpoint): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanCheckpointStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingLanePlanCheckpointRule {
    override fun check(item: PendingLanePlanCheckpoint): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending lane plan checkpoint. */
data class PendingLanePlanCheckpointValidator(var rules: MutableList<PendingLanePlanCheckpointRule>) {
    fun validate(item: PendingLanePlanCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingLanePlanCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLanePlanCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLanePlanCheckpointValidator(): PendingLanePlanCheckpointValidator {
    return PendingLanePlanCheckpointValidator(
        mutableListOf<PendingLanePlanCheckpointRule>(
            PendingLanePlanCheckpointCodeLengthRule(120),
            PendingLanePlanCheckpointVisibleGuardRule(32),
            PendingLanePlanCheckpointLaneLengthRule(80),
            PendingLanePlanCheckpointRequiredCountryRule("pending lane plan checkpoint needs this field"),
            PendingLanePlanCheckpointStartsAtRangeRule(255, 86400),
        ),
    )
}
