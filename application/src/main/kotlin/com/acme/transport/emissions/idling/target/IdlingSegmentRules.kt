package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.normalize

/** A rule a idling segment must pass. Check returns its problems, or none. */
interface IdlingSegmentRule {
    fun check(item: IdlingSegment): MutableList<String>
}

data class IdlingSegmentDescriptionLengthRule(var maxLength: Int) : IdlingSegmentRule {
    override fun check(item: IdlingSegment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingSegmentCeilingRangeRule(var min: Double, var max: Double) : IdlingSegmentRule {
    override fun check(item: IdlingSegment): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class IdlingSegmentNameLengthRule(var maxLength: Int) : IdlingSegmentRule {
    override fun check(item: IdlingSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingSegmentAllowedNameRule(var allowed: MutableList<String>) : IdlingSegmentRule {
    override fun check(item: IdlingSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class IdlingSegmentRequiredCodeRule(var message: String) : IdlingSegmentRule {
    override fun check(item: IdlingSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling segment. */
data class IdlingSegmentValidator(var rules: MutableList<IdlingSegmentRule>) {
    fun validate(item: IdlingSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingSegmentValidator(): IdlingSegmentValidator {
    return IdlingSegmentValidator(
        mutableListOf<IdlingSegmentRule>(
            IdlingSegmentDescriptionLengthRule(1000),
            IdlingSegmentCeilingRangeRule(8.1, 1452.0),
            IdlingSegmentNameLengthRule(80),
            IdlingSegmentAllowedNameRule(mutableListOf<String>("default")),
            IdlingSegmentRequiredCodeRule("idling segment needs this field"),
        ),
    )
}
