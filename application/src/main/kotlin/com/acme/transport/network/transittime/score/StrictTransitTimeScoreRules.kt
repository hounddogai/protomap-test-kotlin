package com.acme.transport.network.transittime.score

/** A rule a strict transit time score must pass. Check returns its problems, or none. */
interface StrictTransitTimeScoreRule {
    fun check(item: StrictTransitTimeScore): MutableList<String>
}

data class StrictTransitTimeScoreRequiredCodeRule(var message: String) : StrictTransitTimeScoreRule {
    override fun check(item: StrictTransitTimeScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeScoreRouteCodeLengthRule(var maxLength: Int) : StrictTransitTimeScoreRule {
    override fun check(item: StrictTransitTimeScore): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeScoreRequiredRouteCodeRule(var message: String) : StrictTransitTimeScoreRule {
    override fun check(item: StrictTransitTimeScore): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeScoreRequiredNameRule(var message: String) : StrictTransitTimeScoreRule {
    override fun check(item: StrictTransitTimeScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeScoreCeilingRangeRule(
    var min: Double,
    var max: Double,
) : StrictTransitTimeScoreRule {
    override fun check(item: StrictTransitTimeScore): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict transit time score. */
data class StrictTransitTimeScoreValidator(var rules: MutableList<StrictTransitTimeScoreRule>) {
    fun validate(item: StrictTransitTimeScore): MutableList<String> {
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

    fun requireValid(item: StrictTransitTimeScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTransitTimeScore>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTransitTimeScoreValidator(): StrictTransitTimeScoreValidator {
    return StrictTransitTimeScoreValidator(
        mutableListOf<StrictTransitTimeScoreRule>(
            StrictTransitTimeScoreRequiredCodeRule("strict transit time score needs an entry"),
            StrictTransitTimeScoreRouteCodeLengthRule(1000),
            StrictTransitTimeScoreRequiredRouteCodeRule("strict transit time score needs this field"),
            StrictTransitTimeScoreRequiredNameRule("strict transit time score needs this field"),
            StrictTransitTimeScoreCeilingRangeRule(1.4, 2442.1),
        ),
    )
}
