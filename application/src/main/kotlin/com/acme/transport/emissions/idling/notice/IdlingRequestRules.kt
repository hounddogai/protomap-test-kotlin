package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.text.normalize

/** A rule a idling request must pass. Check returns its problems, or none. */
interface IdlingRequestRule {
    fun check(item: IdlingRequest): MutableList<String>
}

data class IdlingRequestTitleLengthRule(var maxLength: Int) : IdlingRequestRule {
    override fun check(item: IdlingRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingRequestRequiredLabelRule(var message: String) : IdlingRequestRule {
    override fun check(item: IdlingRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingRequestRevisionRangeRule(var min: Int, var max: Int) : IdlingRequestRule {
    override fun check(item: IdlingRequest): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingRequestAllowedTitleRule(var allowed: MutableList<String>) : IdlingRequestRule {
    override fun check(item: IdlingRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling request. */
data class IdlingRequestValidator(var rules: MutableList<IdlingRequestRule>) {
    fun validate(item: IdlingRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingRequestValidator(): IdlingRequestValidator {
    return IdlingRequestValidator(
        mutableListOf<IdlingRequestRule>(
            IdlingRequestTitleLengthRule(32),
            IdlingRequestRequiredLabelRule("idling request needs this field"),
            IdlingRequestRevisionRangeRule(1000, 1000),
            IdlingRequestAllowedTitleRule(mutableListOf<String>("regional", "default")),
        ),
    )
}
