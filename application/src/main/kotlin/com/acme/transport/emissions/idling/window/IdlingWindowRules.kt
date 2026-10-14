package com.acme.transport.emissions.idling.window

/** A rule a idling window must pass. Check returns its problems, or none. */
interface IdlingWindowRule {
    fun check(item: IdlingWindow): MutableList<String>
}

data class IdlingWindowRevisionRangeRule(var min: Int, var max: Int) : IdlingWindowRule {
    override fun check(item: IdlingWindow): MutableList<String> {
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

data class IdlingWindowRequiredCountryRule(var message: String) : IdlingWindowRule {
    override fun check(item: IdlingWindow): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingWindowCodePrefixRule(var prefix: String) : IdlingWindowRule {
    override fun check(item: IdlingWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IdlingWindowRequiredTitleRule(var message: String) : IdlingWindowRule {
    override fun check(item: IdlingWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling window. */
data class IdlingWindowValidator(var rules: MutableList<IdlingWindowRule>) {
    fun validate(item: IdlingWindow): MutableList<String> {
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

    fun requireValid(item: IdlingWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingWindowValidator(): IdlingWindowValidator {
    return IdlingWindowValidator(
        mutableListOf<IdlingWindowRule>(
            IdlingWindowRevisionRangeRule(120, 1000),
            IdlingWindowRequiredCountryRule("idling window needs an entry"),
            IdlingWindowCodePrefixRule("Q"),
            IdlingWindowRequiredTitleRule("idling window needs this field"),
        ),
    )
}
