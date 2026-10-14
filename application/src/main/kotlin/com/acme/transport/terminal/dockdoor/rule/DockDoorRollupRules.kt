package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.text.normalize

/** A rule a dock door rollup must pass. Check returns its problems, or none. */
interface DockDoorRollupRule {
    fun check(item: DockDoorRollup): MutableList<String>
}

data class DockDoorRollupCodePrefixRule(var prefix: String) : DockDoorRollupRule {
    override fun check(item: DockDoorRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRollupAllowedCodeRule(var allowed: MutableList<String>) : DockDoorRollupRule {
    override fun check(item: DockDoorRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRollupScoreRangeRule(var min: Double, var max: Double) : DockDoorRollupRule {
    override fun check(item: DockDoorRollup): MutableList<String> {
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

/** Runs every rule of a dock door rollup. */
data class DockDoorRollupValidator(var rules: MutableList<DockDoorRollupRule>) {
    fun validate(item: DockDoorRollup): MutableList<String> {
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

    fun requireValid(item: DockDoorRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorRollupValidator(): DockDoorRollupValidator {
    return DockDoorRollupValidator(
        mutableListOf<DockDoorRollupRule>(
            DockDoorRollupCodePrefixRule("ACM"),
            DockDoorRollupAllowedCodeRule(mutableListOf<String>("lenient", "batched")),
            DockDoorRollupScoreRangeRule(6.3, 454.9),
        ),
    )
}
