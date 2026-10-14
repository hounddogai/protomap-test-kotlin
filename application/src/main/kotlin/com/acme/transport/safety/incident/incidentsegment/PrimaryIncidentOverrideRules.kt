package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.normalize

/** A rule a primary incident override must pass. Check returns its problems, or none. */
interface PrimaryIncidentOverrideRule {
    fun check(item: PrimaryIncidentOverride): MutableList<String>
}

data class PrimaryIncidentOverrideTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : PrimaryIncidentOverrideRule {
    override fun check(item: PrimaryIncidentOverride): MutableList<String> {
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

data class PrimaryIncidentOverrideAllowedTitleRule(
    var allowed: MutableList<String>,
) : PrimaryIncidentOverrideRule {
    override fun check(item: PrimaryIncidentOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentOverrideCodeLengthRule(var maxLength: Int) : PrimaryIncidentOverrideRule {
    override fun check(item: PrimaryIncidentOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentOverrideCeilingRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryIncidentOverrideRule {
    override fun check(item: PrimaryIncidentOverride): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentOverrideRequiredCodeRule(var message: String) : PrimaryIncidentOverrideRule {
    override fun check(item: PrimaryIncidentOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary incident override. */
data class PrimaryIncidentOverrideValidator(var rules: MutableList<PrimaryIncidentOverrideRule>) {
    fun validate(item: PrimaryIncidentOverride): MutableList<String> {
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

    fun requireValid(item: PrimaryIncidentOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryIncidentOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryIncidentOverrideValidator(): PrimaryIncidentOverrideValidator {
    return PrimaryIncidentOverrideValidator(
        mutableListOf<PrimaryIncidentOverrideRule>(
            PrimaryIncidentOverrideTagsLimitRule(86400, mutableListOf<String>()),
            PrimaryIncidentOverrideAllowedTitleRule(mutableListOf<String>()),
            PrimaryIncidentOverrideCodeLengthRule(120),
            PrimaryIncidentOverrideCeilingRangeRule(9.8, 4846.9),
            PrimaryIncidentOverrideRequiredCodeRule("primary incident override needs an entry"),
        ),
    )
}
