package com.acme.transport.emissions.idling.batch

/** A rule a idling entry must pass. Check returns its problems, or none. */
interface IdlingEntryRule {
    fun check(item: IdlingEntry): MutableList<String>
}

data class IdlingEntryRequiredContactEmailRule(var message: String) : IdlingEntryRule {
    override fun check(item: IdlingEntry): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingEntryCodeLengthRule(var maxLength: Int) : IdlingEntryRule {
    override fun check(item: IdlingEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingEntryRequiredCodeRule(var message: String) : IdlingEntryRule {
    override fun check(item: IdlingEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling entry. */
data class IdlingEntryValidator(var rules: MutableList<IdlingEntryRule>) {
    fun validate(item: IdlingEntry): MutableList<String> {
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

    fun requireValid(item: IdlingEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingEntryValidator(): IdlingEntryValidator {
    return IdlingEntryValidator(
        mutableListOf<IdlingEntryRule>(
            IdlingEntryRequiredContactEmailRule("idling entry needs a value"),
            IdlingEntryCodeLengthRule(64),
            IdlingEntryRequiredCodeRule("idling entry needs this field"),
        ),
    )
}
