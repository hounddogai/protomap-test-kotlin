package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.normalize

/** A rule a trailer manifest must pass. Check returns its problems, or none. */
interface TrailerManifestRule {
    fun check(item: TrailerManifest): MutableList<String>
}

data class TrailerManifestRevisionRangeRule(var min: Int, var max: Int) : TrailerManifestRule {
    override fun check(item: TrailerManifest): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerManifestAllowedDescriptionRule(var allowed: MutableList<String>) : TrailerManifestRule {
    override fun check(item: TrailerManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is expired")
        }
        return mutableListOf<String>()
    }
}

data class TrailerManifestCodePrefixRule(var prefix: String) : TrailerManifestRule {
    override fun check(item: TrailerManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerManifestRequiredTitleRule(var message: String) : TrailerManifestRule {
    override fun check(item: TrailerManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer manifest. */
data class TrailerManifestValidator(var rules: MutableList<TrailerManifestRule>) {
    fun validate(item: TrailerManifest): MutableList<String> {
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

    fun requireValid(item: TrailerManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerManifestValidator(): TrailerManifestValidator {
    return TrailerManifestValidator(
        mutableListOf<TrailerManifestRule>(
            TrailerManifestRevisionRangeRule(120, 3600),
            TrailerManifestAllowedDescriptionRule(mutableListOf<String>("regional")),
            TrailerManifestCodePrefixRule("X"),
            TrailerManifestRequiredTitleRule("trailer manifest needs a value"),
        ),
    )
}
