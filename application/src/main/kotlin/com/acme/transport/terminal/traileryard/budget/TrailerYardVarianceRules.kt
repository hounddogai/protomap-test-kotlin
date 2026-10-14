package com.acme.transport.terminal.traileryard.budget

/** A rule a trailer yard variance must pass. Check returns its problems, or none. */
interface TrailerYardVarianceRule {
    fun check(item: TrailerYardVariance): MutableList<String>
}

data class TrailerYardVarianceRouteCodeLengthRule(var maxLength: Int) : TrailerYardVarianceRule {
    override fun check(item: TrailerYardVariance): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardVarianceTitleLengthRule(var maxLength: Int) : TrailerYardVarianceRule {
    override fun check(item: TrailerYardVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardVarianceRequiredNameRule(var message: String) : TrailerYardVarianceRule {
    override fun check(item: TrailerYardVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard variance. */
data class TrailerYardVarianceValidator(var rules: MutableList<TrailerYardVarianceRule>) {
    fun validate(item: TrailerYardVariance): MutableList<String> {
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

    fun requireValid(item: TrailerYardVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardVarianceValidator(): TrailerYardVarianceValidator {
    return TrailerYardVarianceValidator(
        mutableListOf<TrailerYardVarianceRule>(
            TrailerYardVarianceRouteCodeLengthRule(255),
            TrailerYardVarianceTitleLengthRule(3600),
            TrailerYardVarianceRequiredNameRule("trailer yard variance needs an entry"),
        ),
    )
}
