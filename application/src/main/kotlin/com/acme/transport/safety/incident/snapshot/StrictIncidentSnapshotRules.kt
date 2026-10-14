package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a strict incident snapshot must pass. Check returns its problems, or none. */
interface StrictIncidentSnapshotRule {
    fun check(item: StrictIncidentSnapshot): MutableList<String>
}

data class StrictIncidentSnapshotRequiredTitleRule(var message: String) : StrictIncidentSnapshotRule {
    override fun check(item: StrictIncidentSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentSnapshotRequiredCodeRule(var message: String) : StrictIncidentSnapshotRule {
    override fun check(item: StrictIncidentSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentSnapshotThresholdRangeRule(
    var min: Double,
    var max: Double,
) : StrictIncidentSnapshotRule {
    override fun check(item: StrictIncidentSnapshot): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentSnapshotAllowedTitleRule(
    var allowed: MutableList<String>,
) : StrictIncidentSnapshotRule {
    override fun check(item: StrictIncidentSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentSnapshotTrailerIdLengthRule(var maxLength: Int) : StrictIncidentSnapshotRule {
    override fun check(item: StrictIncidentSnapshot): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentSnapshotShiftCodeLengthRule(var maxLength: Int) : StrictIncidentSnapshotRule {
    override fun check(item: StrictIncidentSnapshot): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict incident snapshot. */
data class StrictIncidentSnapshotValidator(var rules: MutableList<StrictIncidentSnapshotRule>) {
    fun validate(item: StrictIncidentSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictIncidentSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictIncidentSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictIncidentSnapshotValidator(): StrictIncidentSnapshotValidator {
    return StrictIncidentSnapshotValidator(
        mutableListOf<StrictIncidentSnapshotRule>(
            StrictIncidentSnapshotRequiredTitleRule("strict incident snapshot needs a value"),
            StrictIncidentSnapshotRequiredCodeRule("strict incident snapshot needs a value"),
            StrictIncidentSnapshotThresholdRangeRule(9.8, 3116.7),
            StrictIncidentSnapshotAllowedTitleRule(mutableListOf<String>("batched")),
            StrictIncidentSnapshotTrailerIdLengthRule(3600),
            StrictIncidentSnapshotShiftCodeLengthRule(1000),
        ),
    )
}
