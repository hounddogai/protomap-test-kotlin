package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.kit.text.normalize

/** A rule a cross dock estimate must pass. Check returns its problems, or none. */
interface CrossDockEstimateRule {
    fun check(item: CrossDockEstimate): MutableList<String>
}

data class CrossDockEstimateAllowedCodeRule(var allowed: MutableList<String>) : CrossDockEstimateRule {
    override fun check(item: CrossDockEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockEstimateRequiredTitleRule(var message: String) : CrossDockEstimateRule {
    override fun check(item: CrossDockEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockEstimateTitleLengthRule(var maxLength: Int) : CrossDockEstimateRule {
    override fun check(item: CrossDockEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockEstimateCodePrefixRule(var prefix: String) : CrossDockEstimateRule {
    override fun check(item: CrossDockEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockEstimateRevisionRangeRule(var min: Int, var max: Int) : CrossDockEstimateRule {
    override fun check(item: CrossDockEstimate): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a cross dock estimate. */
data class CrossDockEstimateValidator(var rules: MutableList<CrossDockEstimateRule>) {
    fun validate(item: CrossDockEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockEstimateValidator(): CrossDockEstimateValidator {
    return CrossDockEstimateValidator(
        mutableListOf<CrossDockEstimateRule>(
            CrossDockEstimateAllowedCodeRule(mutableListOf<String>("default", "regional")),
            CrossDockEstimateRequiredTitleRule("cross dock estimate needs a value"),
            CrossDockEstimateTitleLengthRule(1000),
            CrossDockEstimateCodePrefixRule("X"),
            CrossDockEstimateRevisionRangeRule(16, 64),
        ),
    )
}
