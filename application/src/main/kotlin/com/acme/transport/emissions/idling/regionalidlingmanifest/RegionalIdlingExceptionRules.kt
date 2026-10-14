package com.acme.transport.emissions.idling.regionalidlingmanifest

/** A rule a regional idling exception must pass. Check returns its problems, or none. */
interface RegionalIdlingExceptionRule {
    fun check(item: RegionalIdlingException): MutableList<String>
}

data class RegionalIdlingExceptionOwnerIdLengthRule(var maxLength: Int) : RegionalIdlingExceptionRule {
    override fun check(item: RegionalIdlingException): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingExceptionRequiredOwnerIdRule(var message: String) : RegionalIdlingExceptionRule {
    override fun check(item: RegionalIdlingException): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingExceptionStartsAtRangeRule(var min: Int, var max: Int) : RegionalIdlingExceptionRule {
    override fun check(item: RegionalIdlingException): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class RegionalIdlingExceptionRequiredCodeRule(var message: String) : RegionalIdlingExceptionRule {
    override fun check(item: RegionalIdlingException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingExceptionRequiredDescriptionRule(var message: String) : RegionalIdlingExceptionRule {
    override fun check(item: RegionalIdlingException): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional idling exception. */
data class RegionalIdlingExceptionValidator(var rules: MutableList<RegionalIdlingExceptionRule>) {
    fun validate(item: RegionalIdlingException): MutableList<String> {
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

    fun requireValid(item: RegionalIdlingException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIdlingException>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIdlingExceptionValidator(): RegionalIdlingExceptionValidator {
    return RegionalIdlingExceptionValidator(
        mutableListOf<RegionalIdlingExceptionRule>(
            RegionalIdlingExceptionOwnerIdLengthRule(32),
            RegionalIdlingExceptionRequiredOwnerIdRule("regional idling exception needs an entry"),
            RegionalIdlingExceptionStartsAtRangeRule(32, 64),
            RegionalIdlingExceptionRequiredCodeRule("regional idling exception needs this field"),
            RegionalIdlingExceptionRequiredDescriptionRule("regional idling exception needs this field"),
        ),
    )
}
