package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a tiered swap checkpoint must pass. Check returns its problems, or none. */
interface TieredSwapCheckpointRule {
    fun check(item: TieredSwapCheckpoint): MutableList<String>
}

data class TieredSwapCheckpointCodeLengthRule(var maxLength: Int) : TieredSwapCheckpointRule {
    override fun check(item: TieredSwapCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapCheckpointRequiredNameRule(var message: String) : TieredSwapCheckpointRule {
    override fun check(item: TieredSwapCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapCheckpointAllowedNameRule(var allowed: MutableList<String>) : TieredSwapCheckpointRule {
    override fun check(item: TieredSwapCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered swap checkpoint. */
data class TieredSwapCheckpointValidator(var rules: MutableList<TieredSwapCheckpointRule>) {
    fun validate(item: TieredSwapCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredSwapCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSwapCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSwapCheckpointValidator(): TieredSwapCheckpointValidator {
    return TieredSwapCheckpointValidator(
        mutableListOf<TieredSwapCheckpointRule>(
            TieredSwapCheckpointCodeLengthRule(3600),
            TieredSwapCheckpointRequiredNameRule("tiered swap checkpoint needs an entry"),
            TieredSwapCheckpointAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
