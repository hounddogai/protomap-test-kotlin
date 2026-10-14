package com.acme.transport.crewing.swap.digest

/** A rule a rolling swap snapshot must pass. Check returns its problems, or none. */
interface RollingSwapSnapshotRule {
    fun check(item: RollingSwapSnapshot): MutableList<String>
}

data class RollingSwapSnapshotMaximumRangeRule(var min: Double, var max: Double) : RollingSwapSnapshotRule {
    override fun check(item: RollingSwapSnapshot): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class RollingSwapSnapshotRequiredCodeRule(var message: String) : RollingSwapSnapshotRule {
    override fun check(item: RollingSwapSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapSnapshotRequiredTitleRule(var message: String) : RollingSwapSnapshotRule {
    override fun check(item: RollingSwapSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling swap snapshot. */
data class RollingSwapSnapshotValidator(var rules: MutableList<RollingSwapSnapshotRule>) {
    fun validate(item: RollingSwapSnapshot): MutableList<String> {
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

    fun requireValid(item: RollingSwapSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSwapSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSwapSnapshotValidator(): RollingSwapSnapshotValidator {
    return RollingSwapSnapshotValidator(
        mutableListOf<RollingSwapSnapshotRule>(
            RollingSwapSnapshotMaximumRangeRule(7.7, 4499.5),
            RollingSwapSnapshotRequiredCodeRule("rolling swap snapshot needs this field"),
            RollingSwapSnapshotRequiredTitleRule("rolling swap snapshot needs an entry"),
        ),
    )
}
