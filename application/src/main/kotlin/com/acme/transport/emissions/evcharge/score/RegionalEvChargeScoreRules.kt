package com.acme.transport.emissions.evcharge.score

/** A rule a regional ev charge score must pass. Check returns its problems, or none. */
interface RegionalEvChargeScoreRule {
    fun check(item: RegionalEvChargeScore): MutableList<String>
}

data class RegionalEvChargeScoreRevisionRangeRule(var min: Int, var max: Int) : RegionalEvChargeScoreRule {
    override fun check(item: RegionalEvChargeScore): MutableList<String> {
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

data class RegionalEvChargeScoreRequiredCodeRule(var message: String) : RegionalEvChargeScoreRule {
    override fun check(item: RegionalEvChargeScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeScoreCodePrefixRule(var prefix: String) : RegionalEvChargeScoreRule {
    override fun check(item: RegionalEvChargeScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional ev charge score. */
data class RegionalEvChargeScoreValidator(var rules: MutableList<RegionalEvChargeScoreRule>) {
    fun validate(item: RegionalEvChargeScore): MutableList<String> {
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

    fun requireValid(item: RegionalEvChargeScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalEvChargeScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalEvChargeScoreValidator(): RegionalEvChargeScoreValidator {
    return RegionalEvChargeScoreValidator(
        mutableListOf<RegionalEvChargeScoreRule>(
            RegionalEvChargeScoreRevisionRangeRule(255, 3600),
            RegionalEvChargeScoreRequiredCodeRule("regional ev charge score needs a value"),
            RegionalEvChargeScoreCodePrefixRule("REF"),
        ),
    )
}
