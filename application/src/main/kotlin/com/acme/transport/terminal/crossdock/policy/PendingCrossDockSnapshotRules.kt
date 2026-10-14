package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.text.normalize

/** A rule a pending cross dock snapshot must pass. Check returns its problems, or none. */
interface PendingCrossDockSnapshotRule {
    fun check(item: PendingCrossDockSnapshot): MutableList<String>
}

data class PendingCrossDockSnapshotQuantityRangeRule(
    var min: Int,
    var max: Int,
) : PendingCrossDockSnapshotRule {
    override fun check(item: PendingCrossDockSnapshot): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockSnapshotRequiredCodeRule(var message: String) : PendingCrossDockSnapshotRule {
    override fun check(item: PendingCrossDockSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockSnapshotAllowedDockRule(
    var allowed: MutableList<String>,
) : PendingCrossDockSnapshotRule {
    override fun check(item: PendingCrossDockSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockSnapshotApprovedGuardRule(var limit: Int) : PendingCrossDockSnapshotRule {
    override fun check(item: PendingCrossDockSnapshot): MutableList<String> {
        if (item.approved && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending cross dock snapshot. */
data class PendingCrossDockSnapshotValidator(var rules: MutableList<PendingCrossDockSnapshotRule>) {
    fun validate(item: PendingCrossDockSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCrossDockSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCrossDockSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCrossDockSnapshotValidator(): PendingCrossDockSnapshotValidator {
    return PendingCrossDockSnapshotValidator(
        mutableListOf<PendingCrossDockSnapshotRule>(
            PendingCrossDockSnapshotQuantityRangeRule(86400, 86400),
            PendingCrossDockSnapshotRequiredCodeRule("pending cross dock snapshot needs a value"),
            PendingCrossDockSnapshotAllowedDockRule(mutableListOf<String>("strict", "seasonal")),
            PendingCrossDockSnapshotApprovedGuardRule(80),
        ),
    )
}
