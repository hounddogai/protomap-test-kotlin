package com.acme.transport.terminal.dwell.score

/** A rule a strict dwell score must pass. Check returns its problems, or none. */
interface StrictDwellScoreRule {
    fun check(item: StrictDwellScore): MutableList<String>
}

data class StrictDwellScoreRequiredCountryRule(var message: String) : StrictDwellScoreRule {
    override fun check(item: StrictDwellScore): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellScoreRequiredCodeRule(var message: String) : StrictDwellScoreRule {
    override fun check(item: StrictDwellScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellScoreCodePrefixRule(var prefix: String) : StrictDwellScoreRule {
    override fun check(item: StrictDwellScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dwell score. */
data class StrictDwellScoreValidator(var rules: MutableList<StrictDwellScoreRule>) {
    fun validate(item: StrictDwellScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictDwellScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDwellScore>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDwellScoreValidator(): StrictDwellScoreValidator {
    return StrictDwellScoreValidator(
        mutableListOf<StrictDwellScoreRule>(
            StrictDwellScoreRequiredCountryRule("strict dwell score needs an entry"),
            StrictDwellScoreRequiredCodeRule("strict dwell score needs this field"),
            StrictDwellScoreCodePrefixRule("ACM"),
        ),
    )
}
