package com.acme.transport.network.node.adjustment

/** A rule a strict node estimate must pass. Check returns its problems, or none. */
interface StrictNodeEstimateRule {
    fun check(item: StrictNodeEstimate): MutableList<String>
}

data class StrictNodeEstimateTitleLengthRule(var maxLength: Int) : StrictNodeEstimateRule {
    override fun check(item: StrictNodeEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeEstimateRequiredTitleRule(var message: String) : StrictNodeEstimateRule {
    override fun check(item: StrictNodeEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeEstimateOwnerIdLengthRule(var maxLength: Int) : StrictNodeEstimateRule {
    override fun check(item: StrictNodeEstimate): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeEstimateRouteCodeLengthRule(var maxLength: Int) : StrictNodeEstimateRule {
    override fun check(item: StrictNodeEstimate): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeEstimateEtaMinutesRangeRule(var min: Int, var max: Int) : StrictNodeEstimateRule {
    override fun check(item: StrictNodeEstimate): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict node estimate. */
data class StrictNodeEstimateValidator(var rules: MutableList<StrictNodeEstimateRule>) {
    fun validate(item: StrictNodeEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictNodeEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNodeEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNodeEstimateValidator(): StrictNodeEstimateValidator {
    return StrictNodeEstimateValidator(
        mutableListOf<StrictNodeEstimateRule>(
            StrictNodeEstimateTitleLengthRule(16),
            StrictNodeEstimateRequiredTitleRule("strict node estimate needs a value"),
            StrictNodeEstimateOwnerIdLengthRule(16),
            StrictNodeEstimateRouteCodeLengthRule(120),
            StrictNodeEstimateEtaMinutesRangeRule(32, 86400),
        ),
    )
}
