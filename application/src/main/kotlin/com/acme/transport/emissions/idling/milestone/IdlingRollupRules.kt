package com.acme.transport.emissions.idling.milestone

/** A rule a idling rollup must pass. Check returns its problems, or none. */
interface IdlingRollupRule {
    fun check(item: IdlingRollup): MutableList<String>
}

data class IdlingRollupCodePrefixRule(var prefix: String) : IdlingRollupRule {
    override fun check(item: IdlingRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IdlingRollupRequiredCodeRule(var message: String) : IdlingRollupRule {
    override fun check(item: IdlingRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingRollupRequiredUnitRule(var message: String) : IdlingRollupRule {
    override fun check(item: IdlingRollup): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingRollupContactEmailLengthRule(var maxLength: Int) : IdlingRollupRule {
    override fun check(item: IdlingRollup): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling rollup. */
data class IdlingRollupValidator(var rules: MutableList<IdlingRollupRule>) {
    fun validate(item: IdlingRollup): MutableList<String> {
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

    fun requireValid(item: IdlingRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingRollupValidator(): IdlingRollupValidator {
    return IdlingRollupValidator(
        mutableListOf<IdlingRollupRule>(
            IdlingRollupCodePrefixRule("ACM"),
            IdlingRollupRequiredCodeRule("idling rollup needs an entry"),
            IdlingRollupRequiredUnitRule("idling rollup needs this field"),
            IdlingRollupContactEmailLengthRule(80),
        ),
    )
}
