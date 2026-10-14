package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.normalize

/** A rule a pending capacity rollup must pass. Check returns its problems, or none. */
interface PendingCapacityRollupRule {
    fun check(item: PendingCapacityRollup): MutableList<String>
}

data class PendingCapacityRollupPriorityRangeRule(var min: Int, var max: Int) : PendingCapacityRollupRule {
    override fun check(item: PendingCapacityRollup): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class PendingCapacityRollupVisibleGuardRule(var limit: Int) : PendingCapacityRollupRule {
    override fun check(item: PendingCapacityRollup): MutableList<String> {
        if (item.visible && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class PendingCapacityRollupMarginRangeRule(var min: Double, var max: Double) : PendingCapacityRollupRule {
    override fun check(item: PendingCapacityRollup): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class PendingCapacityRollupRequiredTitleRule(var message: String) : PendingCapacityRollupRule {
    override fun check(item: PendingCapacityRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCapacityRollupAllowedCodeRule(var allowed: MutableList<String>) : PendingCapacityRollupRule {
    override fun check(item: PendingCapacityRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending capacity rollup. */
data class PendingCapacityRollupValidator(var rules: MutableList<PendingCapacityRollupRule>) {
    fun validate(item: PendingCapacityRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCapacityRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCapacityRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCapacityRollupValidator(): PendingCapacityRollupValidator {
    return PendingCapacityRollupValidator(
        mutableListOf<PendingCapacityRollupRule>(
            PendingCapacityRollupPriorityRangeRule(32, 86400),
            PendingCapacityRollupVisibleGuardRule(16),
            PendingCapacityRollupMarginRangeRule(3.3, 3771.6),
            PendingCapacityRollupRequiredTitleRule("pending capacity rollup needs this field"),
            PendingCapacityRollupAllowedCodeRule(mutableListOf<String>("weighted", "pending", "pending")),
        ),
    )
}
