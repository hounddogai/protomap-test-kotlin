package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.text.normalize

/** A rule a backhaul rollup must pass. Check returns its problems, or none. */
interface BackhaulRollupRule {
    fun check(item: BackhaulRollup): MutableList<String>
}

data class BackhaulRollupRequiredSegmentRule(var message: String) : BackhaulRollupRule {
    override fun check(item: BackhaulRollup): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulRollupDiscountRangeRule(var min: Double, var max: Double) : BackhaulRollupRule {
    override fun check(item: BackhaulRollup): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class BackhaulRollupAllowedNotesRule(var allowed: MutableList<String>) : BackhaulRollupRule {
    override fun check(item: BackhaulRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is expired")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulRollupCeilingRangeRule(var min: Double, var max: Double) : BackhaulRollupRule {
    override fun check(item: BackhaulRollup): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a backhaul rollup. */
data class BackhaulRollupValidator(var rules: MutableList<BackhaulRollupRule>) {
    fun validate(item: BackhaulRollup): MutableList<String> {
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

    fun requireValid(item: BackhaulRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulRollupValidator(): BackhaulRollupValidator {
    return BackhaulRollupValidator(
        mutableListOf<BackhaulRollupRule>(
            BackhaulRollupRequiredSegmentRule("backhaul rollup needs an entry"),
            BackhaulRollupDiscountRangeRule(0.6, 1782.1),
            BackhaulRollupAllowedNotesRule(mutableListOf<String>("legacy", "adaptive", "scheduled")),
            BackhaulRollupCeilingRangeRule(9.8, 3625.2),
        ),
    )
}
