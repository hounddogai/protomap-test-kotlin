package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.kit.text.normalize

/** A rule a manual incident rollup must pass. Check returns its problems, or none. */
interface ManualIncidentRollupRule {
    fun check(item: ManualIncidentRollup): MutableList<String>
}

data class ManualIncidentRollupRequiredUnitRule(var message: String) : ManualIncidentRollupRule {
    override fun check(item: ManualIncidentRollup): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentRollupBatchSizeRangeRule(var min: Int, var max: Int) : ManualIncidentRollupRule {
    override fun check(item: ManualIncidentRollup): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentRollupAllowedTitleRule(var allowed: MutableList<String>) : ManualIncidentRollupRule {
    override fun check(item: ManualIncidentRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual incident rollup. */
data class ManualIncidentRollupValidator(var rules: MutableList<ManualIncidentRollupRule>) {
    fun validate(item: ManualIncidentRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualIncidentRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIncidentRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIncidentRollupValidator(): ManualIncidentRollupValidator {
    return ManualIncidentRollupValidator(
        mutableListOf<ManualIncidentRollupRule>(
            ManualIncidentRollupRequiredUnitRule("manual incident rollup needs this field"),
            ManualIncidentRollupBatchSizeRangeRule(80, 1000),
            ManualIncidentRollupAllowedTitleRule(mutableListOf<String>("tiered", "default", "batched")),
        ),
    )
}
