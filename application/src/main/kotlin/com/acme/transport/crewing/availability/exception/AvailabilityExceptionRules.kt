package com.acme.transport.crewing.availability.exception

/** A rule a availability exception must pass. Check returns its problems, or none. */
interface AvailabilityExceptionRule {
    fun check(item: AvailabilityException): MutableList<String>
}

data class AvailabilityExceptionTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : AvailabilityExceptionRule {
    override fun check(item: AvailabilityException): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class AvailabilityExceptionRequiredLabelRule(var message: String) : AvailabilityExceptionRule {
    override fun check(item: AvailabilityException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityExceptionRegionLengthRule(var maxLength: Int) : AvailabilityExceptionRule {
    override fun check(item: AvailabilityException): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityExceptionLabelLengthRule(var maxLength: Int) : AvailabilityExceptionRule {
    override fun check(item: AvailabilityException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability exception. */
data class AvailabilityExceptionValidator(var rules: MutableList<AvailabilityExceptionRule>) {
    fun validate(item: AvailabilityException): MutableList<String> {
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

    fun requireValid(item: AvailabilityException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityException>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityExceptionValidator(): AvailabilityExceptionValidator {
    return AvailabilityExceptionValidator(
        mutableListOf<AvailabilityExceptionRule>(
            AvailabilityExceptionTagsLimitRule(80, mutableListOf<String>("fallback")),
            AvailabilityExceptionRequiredLabelRule("availability exception needs a value"),
            AvailabilityExceptionRegionLengthRule(80),
            AvailabilityExceptionLabelLengthRule(1000),
        ),
    )
}
