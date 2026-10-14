package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a pending tender snapshot must pass. Check returns its problems, or none. */
interface PendingTenderSnapshotRule {
    fun check(item: PendingTenderSnapshot): MutableList<String>
}

data class PendingTenderSnapshotAllowedLabelRule(var allowed: MutableList<String>) : PendingTenderSnapshotRule {
    override fun check(item: PendingTenderSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderSnapshotLabelLengthRule(var maxLength: Int) : PendingTenderSnapshotRule {
    override fun check(item: PendingTenderSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderSnapshotRequiredLabelRule(var message: String) : PendingTenderSnapshotRule {
    override fun check(item: PendingTenderSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderSnapshotThresholdRangeRule(
    var min: Double,
    var max: Double,
) : PendingTenderSnapshotRule {
    override fun check(item: PendingTenderSnapshot): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class PendingTenderSnapshotLoadKgRangeRule(var min: Double, var max: Double) : PendingTenderSnapshotRule {
    override fun check(item: PendingTenderSnapshot): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tender snapshot. */
data class PendingTenderSnapshotValidator(var rules: MutableList<PendingTenderSnapshotRule>) {
    fun validate(item: PendingTenderSnapshot): MutableList<String> {
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

    fun requireValid(item: PendingTenderSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTenderSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTenderSnapshotValidator(): PendingTenderSnapshotValidator {
    return PendingTenderSnapshotValidator(
        mutableListOf<PendingTenderSnapshotRule>(
            PendingTenderSnapshotAllowedLabelRule(mutableListOf<String>("manual", "rolling")),
            PendingTenderSnapshotLabelLengthRule(86400),
            PendingTenderSnapshotRequiredLabelRule("pending tender snapshot needs a value"),
            PendingTenderSnapshotThresholdRangeRule(2.3, 4525.9),
            PendingTenderSnapshotLoadKgRangeRule(0.5, 1138.0),
        ),
    )
}
