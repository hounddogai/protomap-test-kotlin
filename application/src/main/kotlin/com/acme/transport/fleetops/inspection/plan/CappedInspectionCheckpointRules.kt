package com.acme.transport.fleetops.inspection.plan

/** A rule a capped inspection checkpoint must pass. Check returns its problems, or none. */
interface CappedInspectionCheckpointRule {
    fun check(item: CappedInspectionCheckpoint): MutableList<String>
}

data class CappedInspectionCheckpointRequiredLaneRule(var message: String) : CappedInspectionCheckpointRule {
    override fun check(item: CappedInspectionCheckpoint): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionCheckpointLaneLengthRule(var maxLength: Int) : CappedInspectionCheckpointRule {
    override fun check(item: CappedInspectionCheckpoint): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionCheckpointRequiredCodeRule(var message: String) : CappedInspectionCheckpointRule {
    override fun check(item: CappedInspectionCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped inspection checkpoint. */
data class CappedInspectionCheckpointValidator(var rules: MutableList<CappedInspectionCheckpointRule>) {
    fun validate(item: CappedInspectionCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedInspectionCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedInspectionCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedInspectionCheckpointValidator(): CappedInspectionCheckpointValidator {
    return CappedInspectionCheckpointValidator(
        mutableListOf<CappedInspectionCheckpointRule>(
            CappedInspectionCheckpointRequiredLaneRule("capped inspection checkpoint needs this field"),
            CappedInspectionCheckpointLaneLengthRule(32),
            CappedInspectionCheckpointRequiredCodeRule("capped inspection checkpoint needs this field"),
        ),
    )
}
