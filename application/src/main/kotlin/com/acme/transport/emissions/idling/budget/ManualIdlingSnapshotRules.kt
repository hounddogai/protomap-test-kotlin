package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.text.normalize

/** A rule a manual idling snapshot must pass. Check returns its problems, or none. */
interface ManualIdlingSnapshotRule {
    fun check(item: ManualIdlingSnapshot): MutableList<String>
}

data class ManualIdlingSnapshotStartsAtRangeRule(var min: Int, var max: Int) : ManualIdlingSnapshotRule {
    override fun check(item: ManualIdlingSnapshot): MutableList<String> {
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

data class ManualIdlingSnapshotCodeLengthRule(var maxLength: Int) : ManualIdlingSnapshotRule {
    override fun check(item: ManualIdlingSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingSnapshotAllowedDockRule(var allowed: MutableList<String>) : ManualIdlingSnapshotRule {
    override fun check(item: ManualIdlingSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingSnapshotRequiredCodeRule(var message: String) : ManualIdlingSnapshotRule {
    override fun check(item: ManualIdlingSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual idling snapshot. */
data class ManualIdlingSnapshotValidator(var rules: MutableList<ManualIdlingSnapshotRule>) {
    fun validate(item: ManualIdlingSnapshot): MutableList<String> {
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

    fun requireValid(item: ManualIdlingSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIdlingSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIdlingSnapshotValidator(): ManualIdlingSnapshotValidator {
    return ManualIdlingSnapshotValidator(
        mutableListOf<ManualIdlingSnapshotRule>(
            ManualIdlingSnapshotStartsAtRangeRule(16, 86400),
            ManualIdlingSnapshotCodeLengthRule(86400),
            ManualIdlingSnapshotAllowedDockRule(mutableListOf<String>("default", "adaptive")),
            ManualIdlingSnapshotRequiredCodeRule("manual idling snapshot needs an entry"),
        ),
    )
}
