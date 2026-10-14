package com.acme.transport.fleetops.tire.profile

/** A rule a tire score must pass. Check returns its problems, or none. */
interface TireScoreRule {
    fun check(item: TireScore): MutableList<String>
}

data class TireScoreCodePrefixRule(var prefix: String) : TireScoreRule {
    override fun check(item: TireScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireScoreCodeLengthRule(var maxLength: Int) : TireScoreRule {
    override fun check(item: TireScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireScoreRequiredPhoneRule(var message: String) : TireScoreRule {
    override fun check(item: TireScore): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire score. */
data class TireScoreValidator(var rules: MutableList<TireScoreRule>) {
    fun validate(item: TireScore): MutableList<String> {
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

    fun requireValid(item: TireScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireScore>): MutableMap<String, MutableList<String>> {
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

fun defaultTireScoreValidator(): TireScoreValidator {
    return TireScoreValidator(
        mutableListOf<TireScoreRule>(
            TireScoreCodePrefixRule("REF"),
            TireScoreCodeLengthRule(64),
            TireScoreRequiredPhoneRule("tire score needs an entry"),
        ),
    )
}
