package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.normalize

/** A rule a clearance profile must pass. Check returns its problems, or none. */
interface ClearanceProfileRule {
    fun check(item: ClearanceProfile): MutableList<String>
}

data class ClearanceProfileTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ClearanceProfileRule {
    override fun check(item: ClearanceProfile): MutableList<String> {
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

data class ClearanceProfileAllowedDriverIdRule(var allowed: MutableList<String>) : ClearanceProfileRule {
    override fun check(item: ClearanceProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceProfileLabelLengthRule(var maxLength: Int) : ClearanceProfileRule {
    override fun check(item: ClearanceProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceProfileDriverIdLengthRule(var maxLength: Int) : ClearanceProfileRule {
    override fun check(item: ClearanceProfile): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance profile. */
data class ClearanceProfileValidator(var rules: MutableList<ClearanceProfileRule>) {
    fun validate(item: ClearanceProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceProfileValidator(): ClearanceProfileValidator {
    return ClearanceProfileValidator(
        mutableListOf<ClearanceProfileRule>(
            ClearanceProfileTagsLimitRule(255, mutableListOf<String>("primary")),
            ClearanceProfileAllowedDriverIdRule(mutableListOf<String>("manual", "lenient")),
            ClearanceProfileLabelLengthRule(80),
            ClearanceProfileDriverIdLengthRule(1000),
        ),
    )
}
