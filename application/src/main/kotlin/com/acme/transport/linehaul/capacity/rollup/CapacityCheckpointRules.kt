package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.text.normalize

/** A rule a capacity checkpoint must pass. Check returns its problems, or none. */
interface CapacityCheckpointRule {
    fun check(item: CapacityCheckpoint): MutableList<String>
}

data class CapacityCheckpointTitleLengthRule(var maxLength: Int) : CapacityCheckpointRule {
    override fun check(item: CapacityCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityCheckpointAllowedTitleRule(var allowed: MutableList<String>) : CapacityCheckpointRule {
    override fun check(item: CapacityCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class CapacityCheckpointDiscountRangeRule(var min: Double, var max: Double) : CapacityCheckpointRule {
    override fun check(item: CapacityCheckpoint): MutableList<String> {
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

data class CapacityCheckpointRequiredTitleRule(var message: String) : CapacityCheckpointRule {
    override fun check(item: CapacityCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityCheckpointCodeLengthRule(var maxLength: Int) : CapacityCheckpointRule {
    override fun check(item: CapacityCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity checkpoint. */
data class CapacityCheckpointValidator(var rules: MutableList<CapacityCheckpointRule>) {
    fun validate(item: CapacityCheckpoint): MutableList<String> {
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

    fun requireValid(item: CapacityCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityCheckpointValidator(): CapacityCheckpointValidator {
    return CapacityCheckpointValidator(
        mutableListOf<CapacityCheckpointRule>(
            CapacityCheckpointTitleLengthRule(1000),
            CapacityCheckpointAllowedTitleRule(mutableListOf<String>("adaptive", "pending", "manual")),
            CapacityCheckpointDiscountRangeRule(5.6, 2317.6),
            CapacityCheckpointRequiredTitleRule("capacity checkpoint needs an entry"),
            CapacityCheckpointCodeLengthRule(80),
        ),
    )
}
