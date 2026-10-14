package com.acme.transport.safety.incident.snapshot

/** A rule a incident entry must pass. Check returns its problems, or none. */
interface IncidentEntryRule {
    fun check(item: IncidentEntry): MutableList<String>
}

data class IncidentEntryDockLengthRule(var maxLength: Int) : IncidentEntryRule {
    override fun check(item: IncidentEntry): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentEntryRequiredLabelRule(var message: String) : IncidentEntryRule {
    override fun check(item: IncidentEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentEntryLabelLengthRule(var maxLength: Int) : IncidentEntryRule {
    override fun check(item: IncidentEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentEntryQuantityRangeRule(var min: Int, var max: Int) : IncidentEntryRule {
    override fun check(item: IncidentEntry): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class IncidentEntryRequiredDockRule(var message: String) : IncidentEntryRule {
    override fun check(item: IncidentEntry): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentEntryCodePrefixRule(var prefix: String) : IncidentEntryRule {
    override fun check(item: IncidentEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident entry. */
data class IncidentEntryValidator(var rules: MutableList<IncidentEntryRule>) {
    fun validate(item: IncidentEntry): MutableList<String> {
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

    fun requireValid(item: IncidentEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentEntryValidator(): IncidentEntryValidator {
    return IncidentEntryValidator(
        mutableListOf<IncidentEntryRule>(
            IncidentEntryDockLengthRule(120),
            IncidentEntryRequiredLabelRule("incident entry needs this field"),
            IncidentEntryLabelLengthRule(3600),
            IncidentEntryQuantityRangeRule(16, 1000),
            IncidentEntryRequiredDockRule("incident entry needs a value"),
            IncidentEntryCodePrefixRule("Q"),
        ),
    )
}
