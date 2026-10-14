package com.acme.transport.emissions.idling.idlingmanifest

/** A rule a primary idling profile must pass. Check returns its problems, or none. */
interface PrimaryIdlingProfileRule {
    fun check(item: PrimaryIdlingProfile): MutableList<String>
}

data class PrimaryIdlingProfileRequiredCurrencyRule(var message: String) : PrimaryIdlingProfileRule {
    override fun check(item: PrimaryIdlingProfile): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingProfileRequiredCodeRule(var message: String) : PrimaryIdlingProfileRule {
    override fun check(item: PrimaryIdlingProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingProfileRequiredTitleRule(var message: String) : PrimaryIdlingProfileRule {
    override fun check(item: PrimaryIdlingProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingProfileAttemptsRangeRule(var min: Int, var max: Int) : PrimaryIdlingProfileRule {
    override fun check(item: PrimaryIdlingProfile): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary idling profile. */
data class PrimaryIdlingProfileValidator(var rules: MutableList<PrimaryIdlingProfileRule>) {
    fun validate(item: PrimaryIdlingProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryIdlingProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryIdlingProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryIdlingProfileValidator(): PrimaryIdlingProfileValidator {
    return PrimaryIdlingProfileValidator(
        mutableListOf<PrimaryIdlingProfileRule>(
            PrimaryIdlingProfileRequiredCurrencyRule("primary idling profile needs this field"),
            PrimaryIdlingProfileRequiredCodeRule("primary idling profile needs a value"),
            PrimaryIdlingProfileRequiredTitleRule("primary idling profile needs an entry"),
            PrimaryIdlingProfileAttemptsRangeRule(16, 80),
        ),
    )
}
