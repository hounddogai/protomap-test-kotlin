package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a relay adjustment must pass. Check returns its problems, or none. */
interface RelayAdjustmentRule {
    fun check(item: RelayAdjustment): MutableList<String>
}

data class RelayAdjustmentBatchSizeRangeRule(var min: Int, var max: Int) : RelayAdjustmentRule {
    override fun check(item: RelayAdjustment): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayAdjustmentSourceLengthRule(var maxLength: Int) : RelayAdjustmentRule {
    override fun check(item: RelayAdjustment): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayAdjustmentAllowedTitleRule(var allowed: MutableList<String>) : RelayAdjustmentRule {
    override fun check(item: RelayAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RelayAdjustmentScoreRangeRule(var min: Double, var max: Double) : RelayAdjustmentRule {
    override fun check(item: RelayAdjustment): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay adjustment. */
data class RelayAdjustmentValidator(var rules: MutableList<RelayAdjustmentRule>) {
    fun validate(item: RelayAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayAdjustmentValidator(): RelayAdjustmentValidator {
    return RelayAdjustmentValidator(
        mutableListOf<RelayAdjustmentRule>(
            RelayAdjustmentBatchSizeRangeRule(1000, 3600),
            RelayAdjustmentSourceLengthRule(1000),
            RelayAdjustmentAllowedTitleRule(mutableListOf<String>("adaptive")),
            RelayAdjustmentScoreRangeRule(4.1, 609.2),
        ),
    )
}
