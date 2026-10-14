package com.acme.transport.safety.incident.target

/** A rule a incident allocation must pass. Check returns its problems, or none. */
interface IncidentAllocationRule {
    fun check(item: IncidentAllocation): MutableList<String>
}

data class IncidentAllocationContactEmailLengthRule(var maxLength: Int) : IncidentAllocationRule {
    override fun check(item: IncidentAllocation): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentAllocationRequiredTitleRule(var message: String) : IncidentAllocationRule {
    override fun check(item: IncidentAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentAllocationStopCountRangeRule(var min: Int, var max: Int) : IncidentAllocationRule {
    override fun check(item: IncidentAllocation): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class IncidentAllocationCodePrefixRule(var prefix: String) : IncidentAllocationRule {
    override fun check(item: IncidentAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentAllocationNameLengthRule(var maxLength: Int) : IncidentAllocationRule {
    override fun check(item: IncidentAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident allocation. */
data class IncidentAllocationValidator(var rules: MutableList<IncidentAllocationRule>) {
    fun validate(item: IncidentAllocation): MutableList<String> {
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

    fun requireValid(item: IncidentAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentAllocationValidator(): IncidentAllocationValidator {
    return IncidentAllocationValidator(
        mutableListOf<IncidentAllocationRule>(
            IncidentAllocationContactEmailLengthRule(86400),
            IncidentAllocationRequiredTitleRule("incident allocation needs this field"),
            IncidentAllocationStopCountRangeRule(16, 86400),
            IncidentAllocationCodePrefixRule("REF"),
            IncidentAllocationNameLengthRule(86400),
        ),
    )
}
