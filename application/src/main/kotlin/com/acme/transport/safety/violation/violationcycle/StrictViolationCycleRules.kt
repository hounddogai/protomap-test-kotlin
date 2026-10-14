package com.acme.transport.safety.violation.violationcycle

/** A rule a strict violation cycle must pass. Check returns its problems, or none. */
interface StrictViolationCycleRule {
    fun check(item: StrictViolationCycle): MutableList<String>
}

data class StrictViolationCycleNameLengthRule(var maxLength: Int) : StrictViolationCycleRule {
    override fun check(item: StrictViolationCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationCycleRequiredLocaleRule(var message: String) : StrictViolationCycleRule {
    override fun check(item: StrictViolationCycle): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationCycleRequiredOwnerIdRule(var message: String) : StrictViolationCycleRule {
    override fun check(item: StrictViolationCycle): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict violation cycle. */
data class StrictViolationCycleValidator(var rules: MutableList<StrictViolationCycleRule>) {
    fun validate(item: StrictViolationCycle): MutableList<String> {
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

    fun requireValid(item: StrictViolationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictViolationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictViolationCycleValidator(): StrictViolationCycleValidator {
    return StrictViolationCycleValidator(
        mutableListOf<StrictViolationCycleRule>(
            StrictViolationCycleNameLengthRule(120),
            StrictViolationCycleRequiredLocaleRule("strict violation cycle needs this field"),
            StrictViolationCycleRequiredOwnerIdRule("strict violation cycle needs an entry"),
        ),
    )
}
