package com.acme.transport.emissions.idling.baseline

/** A rule a idling mapping must pass. Check returns its problems, or none. */
interface IdlingMappingRule {
    fun check(item: IdlingMapping): MutableList<String>
}

data class IdlingMappingRequiredTitleRule(var message: String) : IdlingMappingRule {
    override fun check(item: IdlingMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingMappingRequiredLaneRule(var message: String) : IdlingMappingRule {
    override fun check(item: IdlingMapping): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingMappingCodeLengthRule(var maxLength: Int) : IdlingMappingRule {
    override fun check(item: IdlingMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling mapping. */
data class IdlingMappingValidator(var rules: MutableList<IdlingMappingRule>) {
    fun validate(item: IdlingMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingMappingValidator(): IdlingMappingValidator {
    return IdlingMappingValidator(
        mutableListOf<IdlingMappingRule>(
            IdlingMappingRequiredTitleRule("idling mapping needs this field"),
            IdlingMappingRequiredLaneRule("idling mapping needs an entry"),
            IdlingMappingCodeLengthRule(64),
        ),
    )
}
