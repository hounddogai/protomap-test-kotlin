package com.acme.transport.safety.incident.proposal

/** A rule a incident tier must pass. Check returns its problems, or none. */
interface IncidentTierRule {
    fun check(item: IncidentTier): MutableList<String>
}

data class IncidentTierCodePrefixRule(var prefix: String) : IncidentTierRule {
    override fun check(item: IncidentTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentTierLabelLengthRule(var maxLength: Int) : IncidentTierRule {
    override fun check(item: IncidentTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentTierRequiredLabelRule(var message: String) : IncidentTierRule {
    override fun check(item: IncidentTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentTierCodeLengthRule(var maxLength: Int) : IncidentTierRule {
    override fun check(item: IncidentTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident tier. */
data class IncidentTierValidator(var rules: MutableList<IncidentTierRule>) {
    fun validate(item: IncidentTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentTier>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentTierValidator(): IncidentTierValidator {
    return IncidentTierValidator(
        mutableListOf<IncidentTierRule>(
            IncidentTierCodePrefixRule("X"),
            IncidentTierLabelLengthRule(64),
            IncidentTierRequiredLabelRule("incident tier needs a value"),
            IncidentTierCodeLengthRule(86400),
        ),
    )
}
