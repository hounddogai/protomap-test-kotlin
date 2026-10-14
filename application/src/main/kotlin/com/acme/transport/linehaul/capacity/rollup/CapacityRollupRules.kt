package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.text.normalize

/** A rule a capacity rollup must pass. Check returns its problems, or none. */
interface CapacityRollupRule {
    fun check(item: CapacityRollup): MutableList<String>
}

data class CapacityRollupRequiredCodeRule(var message: String) : CapacityRollupRule {
    override fun check(item: CapacityRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityRollupFloorValueRangeRule(var min: Double, var max: Double) : CapacityRollupRule {
    override fun check(item: CapacityRollup): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class CapacityRollupAllowedCodeRule(var allowed: MutableList<String>) : CapacityRollupRule {
    override fun check(item: CapacityRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CapacityRollupActiveGuardRule(var limit: Double) : CapacityRollupRule {
    override fun check(item: CapacityRollup): MutableList<String> {
        if (item.active && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class CapacityRollupUpdatedAtRangeRule(var min: Int, var max: Int) : CapacityRollupRule {
    override fun check(item: CapacityRollup): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capacity rollup. */
data class CapacityRollupValidator(var rules: MutableList<CapacityRollupRule>) {
    fun validate(item: CapacityRollup): MutableList<String> {
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

    fun requireValid(item: CapacityRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityRollupValidator(): CapacityRollupValidator {
    return CapacityRollupValidator(
        mutableListOf<CapacityRollupRule>(
            CapacityRollupRequiredCodeRule("capacity rollup needs an entry"),
            CapacityRollupFloorValueRangeRule(1.7, 212.7),
            CapacityRollupAllowedCodeRule(mutableListOf<String>()),
            CapacityRollupActiveGuardRule(0.4),
            CapacityRollupUpdatedAtRangeRule(32, 80),
        ),
    )
}
