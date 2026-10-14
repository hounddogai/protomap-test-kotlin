package com.acme.transport.safety.incident.baseline

/** A rule a incident override must pass. Check returns its problems, or none. */
interface IncidentOverrideRule {
    fun check(item: IncidentOverride): MutableList<String>
}

data class IncidentOverrideCodePrefixRule(var prefix: String) : IncidentOverrideRule {
    override fun check(item: IncidentOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentOverrideAmountRangeRule(var min: Double, var max: Double) : IncidentOverrideRule {
    override fun check(item: IncidentOverride): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentOverrideNotesLengthRule(var maxLength: Int) : IncidentOverrideRule {
    override fun check(item: IncidentOverride): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentOverrideCodeLengthRule(var maxLength: Int) : IncidentOverrideRule {
    override fun check(item: IncidentOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentOverrideRequiredCodeRule(var message: String) : IncidentOverrideRule {
    override fun check(item: IncidentOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident override. */
data class IncidentOverrideValidator(var rules: MutableList<IncidentOverrideRule>) {
    fun validate(item: IncidentOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentOverrideValidator(): IncidentOverrideValidator {
    return IncidentOverrideValidator(
        mutableListOf<IncidentOverrideRule>(
            IncidentOverrideCodePrefixRule("ACM"),
            IncidentOverrideAmountRangeRule(6.7, 3479.4),
            IncidentOverrideNotesLengthRule(32),
            IncidentOverrideCodeLengthRule(120),
            IncidentOverrideRequiredCodeRule("incident override needs an entry"),
        ),
    )
}
