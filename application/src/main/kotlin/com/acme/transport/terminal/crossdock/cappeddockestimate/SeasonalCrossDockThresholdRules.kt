package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.text.normalize

/** A rule a seasonal cross dock threshold must pass. Check returns its problems, or none. */
interface SeasonalCrossDockThresholdRule {
    fun check(item: SeasonalCrossDockThreshold): MutableList<String>
}

data class SeasonalCrossDockThresholdRequiredCodeRule(var message: String) : SeasonalCrossDockThresholdRule {
    override fun check(item: SeasonalCrossDockThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCrossDockThresholdAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalCrossDockThresholdRule {
    override fun check(item: SeasonalCrossDockThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCrossDockThresholdCodePrefixRule(var prefix: String) : SeasonalCrossDockThresholdRule {
    override fun check(item: SeasonalCrossDockThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCrossDockThresholdDockLengthRule(var maxLength: Int) : SeasonalCrossDockThresholdRule {
    override fun check(item: SeasonalCrossDockThreshold): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal cross dock threshold. */
data class SeasonalCrossDockThresholdValidator(var rules: MutableList<SeasonalCrossDockThresholdRule>) {
    fun validate(item: SeasonalCrossDockThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCrossDockThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCrossDockThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCrossDockThresholdValidator(): SeasonalCrossDockThresholdValidator {
    return SeasonalCrossDockThresholdValidator(
        mutableListOf<SeasonalCrossDockThresholdRule>(
            SeasonalCrossDockThresholdRequiredCodeRule("seasonal cross dock threshold needs this field"),
            SeasonalCrossDockThresholdAllowedCodeRule(mutableListOf<String>("batched")),
            SeasonalCrossDockThresholdCodePrefixRule("REF"),
            SeasonalCrossDockThresholdDockLengthRule(64),
        ),
    )
}
