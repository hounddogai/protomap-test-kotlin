package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a incident snapshot must pass. Check returns its problems, or none. */
interface IncidentSnapshotRule {
    fun check(item: IncidentSnapshot): MutableList<String>
}

data class IncidentSnapshotSequenceRangeRule(var min: Int, var max: Int) : IncidentSnapshotRule {
    override fun check(item: IncidentSnapshot): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class IncidentSnapshotRequiredCodeRule(var message: String) : IncidentSnapshotRule {
    override fun check(item: IncidentSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentSnapshotCodeLengthRule(var maxLength: Int) : IncidentSnapshotRule {
    override fun check(item: IncidentSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentSnapshotAllowedTitleRule(var allowed: MutableList<String>) : IncidentSnapshotRule {
    override fun check(item: IncidentSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class IncidentSnapshotCarrierLengthRule(var maxLength: Int) : IncidentSnapshotRule {
    override fun check(item: IncidentSnapshot): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident snapshot. */
data class IncidentSnapshotValidator(var rules: MutableList<IncidentSnapshotRule>) {
    fun validate(item: IncidentSnapshot): MutableList<String> {
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

    fun requireValid(item: IncidentSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentSnapshotValidator(): IncidentSnapshotValidator {
    return IncidentSnapshotValidator(
        mutableListOf<IncidentSnapshotRule>(
            IncidentSnapshotSequenceRangeRule(1000, 86400),
            IncidentSnapshotRequiredCodeRule("incident snapshot needs this field"),
            IncidentSnapshotCodeLengthRule(80),
            IncidentSnapshotAllowedTitleRule(mutableListOf<String>()),
            IncidentSnapshotCarrierLengthRule(255),
        ),
    )
}
