package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.normalize

/** A rule a tiered cross dock baseline must pass. Check returns its problems, or none. */
interface TieredCrossDockBaselineRule {
    fun check(item: TieredCrossDockBaseline): MutableList<String>
}

data class TieredCrossDockBaselineNotesLengthRule(var maxLength: Int) : TieredCrossDockBaselineRule {
    override fun check(item: TieredCrossDockBaseline): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockBaselineCodePrefixRule(var prefix: String) : TieredCrossDockBaselineRule {
    override fun check(item: TieredCrossDockBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockBaselineRequiredNotesRule(var message: String) : TieredCrossDockBaselineRule {
    override fun check(item: TieredCrossDockBaseline): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockBaselineAllowedSourceRule(
    var allowed: MutableList<String>,
) : TieredCrossDockBaselineRule {
    override fun check(item: TieredCrossDockBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockBaselineStartsAtRangeRule(var min: Int, var max: Int) : TieredCrossDockBaselineRule {
    override fun check(item: TieredCrossDockBaseline): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered cross dock baseline. */
data class TieredCrossDockBaselineValidator(var rules: MutableList<TieredCrossDockBaselineRule>) {
    fun validate(item: TieredCrossDockBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCrossDockBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCrossDockBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCrossDockBaselineValidator(): TieredCrossDockBaselineValidator {
    return TieredCrossDockBaselineValidator(
        mutableListOf<TieredCrossDockBaselineRule>(
            TieredCrossDockBaselineNotesLengthRule(86400),
            TieredCrossDockBaselineCodePrefixRule("Q"),
            TieredCrossDockBaselineRequiredNotesRule("tiered cross dock baseline needs an entry"),
            TieredCrossDockBaselineAllowedSourceRule(mutableListOf<String>("lenient", "strict")),
            TieredCrossDockBaselineStartsAtRangeRule(64, 86400),
        ),
    )
}
