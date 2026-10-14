package com.acme.transport.terminal.crossdock.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a cross dock record must pass. Check returns its problems, or none. */
interface CrossDockRecordRule {
    fun check(item: CrossDockRecord): MutableList<String>
}

data class CrossDockRecordLimitRangeRule(var min: Int, var max: Int) : CrossDockRecordRule {
    override fun check(item: CrossDockRecord): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class CrossDockRecordCodeLengthRule(var maxLength: Int) : CrossDockRecordRule {
    override fun check(item: CrossDockRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRecordRequiredTitleRule(var message: String) : CrossDockRecordRule {
    override fun check(item: CrossDockRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRecordScoreRangeRule(var min: Double, var max: Double) : CrossDockRecordRule {
    override fun check(item: CrossDockRecord): MutableList<String> {
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

data class CrossDockRecordRequiredCodeRule(var message: String) : CrossDockRecordRule {
    override fun check(item: CrossDockRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRecordAllowedTitleRule(var allowed: MutableList<String>) : CrossDockRecordRule {
    override fun check(item: CrossDockRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock record. */
data class CrossDockRecordValidator(var rules: MutableList<CrossDockRecordRule>) {
    fun validate(item: CrossDockRecord): MutableList<String> {
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

    fun requireValid(item: CrossDockRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockRecordValidator(): CrossDockRecordValidator {
    return CrossDockRecordValidator(
        mutableListOf<CrossDockRecordRule>(
            CrossDockRecordLimitRangeRule(120, 1000),
            CrossDockRecordCodeLengthRule(255),
            CrossDockRecordRequiredTitleRule("cross dock record needs an entry"),
            CrossDockRecordScoreRangeRule(2.6, 4837.3),
            CrossDockRecordRequiredCodeRule("cross dock record needs a value"),
            CrossDockRecordAllowedTitleRule(mutableListOf<String>("capped", "adaptive")),
        ),
    )
}
