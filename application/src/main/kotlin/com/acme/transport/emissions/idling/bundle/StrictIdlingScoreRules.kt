package com.acme.transport.emissions.idling.bundle

/** A rule a strict idling score must pass. Check returns its problems, or none. */
interface StrictIdlingScoreRule {
    fun check(item: StrictIdlingScore): MutableList<String>
}

data class StrictIdlingScoreCodePrefixRule(var prefix: String) : StrictIdlingScoreRule {
    override fun check(item: StrictIdlingScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingScoreRequiredChannelRule(var message: String) : StrictIdlingScoreRule {
    override fun check(item: StrictIdlingScore): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingScoreCodeLengthRule(var maxLength: Int) : StrictIdlingScoreRule {
    override fun check(item: StrictIdlingScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict idling score. */
data class StrictIdlingScoreValidator(var rules: MutableList<StrictIdlingScoreRule>) {
    fun validate(item: StrictIdlingScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictIdlingScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictIdlingScore>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictIdlingScoreValidator(): StrictIdlingScoreValidator {
    return StrictIdlingScoreValidator(
        mutableListOf<StrictIdlingScoreRule>(
            StrictIdlingScoreCodePrefixRule("X"),
            StrictIdlingScoreRequiredChannelRule("strict idling score needs this field"),
            StrictIdlingScoreCodeLengthRule(32),
        ),
    )
}
