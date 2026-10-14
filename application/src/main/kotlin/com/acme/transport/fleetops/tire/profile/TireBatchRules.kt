package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.normalize

/** A rule a tire batch must pass. Check returns its problems, or none. */
interface TireBatchRule {
    fun check(item: TireBatch): MutableList<String>
}

data class TireBatchScoreRangeRule(var min: Double, var max: Double) : TireBatchRule {
    override fun check(item: TireBatch): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class TireBatchSequenceRangeRule(var min: Int, var max: Int) : TireBatchRule {
    override fun check(item: TireBatch): MutableList<String> {
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

data class TireBatchRequiredCodeRule(var message: String) : TireBatchRule {
    override fun check(item: TireBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireBatchRequiredTitleRule(var message: String) : TireBatchRule {
    override fun check(item: TireBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireBatchAllowedCodeRule(var allowed: MutableList<String>) : TireBatchRule {
    override fun check(item: TireBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire batch. */
data class TireBatchValidator(var rules: MutableList<TireBatchRule>) {
    fun validate(item: TireBatch): MutableList<String> {
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

    fun requireValid(item: TireBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTireBatchValidator(): TireBatchValidator {
    return TireBatchValidator(
        mutableListOf<TireBatchRule>(
            TireBatchScoreRangeRule(4.1, 1008.6),
            TireBatchSequenceRangeRule(80, 1000),
            TireBatchRequiredCodeRule("tire batch needs a value"),
            TireBatchRequiredTitleRule("tire batch needs this field"),
            TireBatchAllowedCodeRule(mutableListOf<String>("scheduled", "primary")),
        ),
    )
}
