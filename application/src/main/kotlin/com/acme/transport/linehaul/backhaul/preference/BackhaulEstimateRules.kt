package com.acme.transport.linehaul.backhaul.preference

/** A rule a backhaul estimate must pass. Check returns its problems, or none. */
interface BackhaulEstimateRule {
    fun check(item: BackhaulEstimate): MutableList<String>
}

data class BackhaulEstimateSummaryLengthRule(var maxLength: Int) : BackhaulEstimateRule {
    override fun check(item: BackhaulEstimate): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulEstimateNameLengthRule(var maxLength: Int) : BackhaulEstimateRule {
    override fun check(item: BackhaulEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulEstimateCodePrefixRule(var prefix: String) : BackhaulEstimateRule {
    override fun check(item: BackhaulEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul estimate. */
data class BackhaulEstimateValidator(var rules: MutableList<BackhaulEstimateRule>) {
    fun validate(item: BackhaulEstimate): MutableList<String> {
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

    fun requireValid(item: BackhaulEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulEstimateValidator(): BackhaulEstimateValidator {
    return BackhaulEstimateValidator(
        mutableListOf<BackhaulEstimateRule>(
            BackhaulEstimateSummaryLengthRule(3600),
            BackhaulEstimateNameLengthRule(1000),
            BackhaulEstimateCodePrefixRule("Q"),
        ),
    )
}
