package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.text.normalize

/** A rule a default cutoff estimate must pass. Check returns its problems, or none. */
interface DefaultCutoffEstimateRule {
    fun check(item: DefaultCutoffEstimate): MutableList<String>
}

data class DefaultCutoffEstimateRequiredCodeRule(var message: String) : DefaultCutoffEstimateRule {
    override fun check(item: DefaultCutoffEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffEstimateTitleLengthRule(var maxLength: Int) : DefaultCutoffEstimateRule {
    override fun check(item: DefaultCutoffEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffEstimateAllowedTitleRule(var allowed: MutableList<String>) : DefaultCutoffEstimateRule {
    override fun check(item: DefaultCutoffEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default cutoff estimate. */
data class DefaultCutoffEstimateValidator(var rules: MutableList<DefaultCutoffEstimateRule>) {
    fun validate(item: DefaultCutoffEstimate): MutableList<String> {
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

    fun requireValid(item: DefaultCutoffEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCutoffEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCutoffEstimateValidator(): DefaultCutoffEstimateValidator {
    return DefaultCutoffEstimateValidator(
        mutableListOf<DefaultCutoffEstimateRule>(
            DefaultCutoffEstimateRequiredCodeRule("default cutoff estimate needs a value"),
            DefaultCutoffEstimateTitleLengthRule(16),
            DefaultCutoffEstimateAllowedTitleRule(mutableListOf<String>("seasonal")),
        ),
    )
}
