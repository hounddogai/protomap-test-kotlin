package com.acme.transport.linehaul.trailer.review

/** A rule a trailer baseline must pass. Check returns its problems, or none. */
interface TrailerBaselineRule {
    fun check(item: TrailerBaseline): MutableList<String>
}

data class TrailerBaselineRequiredCodeRule(var message: String) : TrailerBaselineRule {
    override fun check(item: TrailerBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerBaselineRateRangeRule(var min: Double, var max: Double) : TrailerBaselineRule {
    override fun check(item: TrailerBaseline): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class TrailerBaselinePinnedGuardRule(var limit: Int) : TrailerBaselineRule {
    override fun check(item: TrailerBaseline): MutableList<String> {
        if (item.pinned && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer baseline. */
data class TrailerBaselineValidator(var rules: MutableList<TrailerBaselineRule>) {
    fun validate(item: TrailerBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerBaselineValidator(): TrailerBaselineValidator {
    return TrailerBaselineValidator(
        mutableListOf<TrailerBaselineRule>(
            TrailerBaselineRequiredCodeRule("trailer baseline needs an entry"),
            TrailerBaselineRateRangeRule(8.8, 2856.4),
            TrailerBaselinePinnedGuardRule(3600),
        ),
    )
}
