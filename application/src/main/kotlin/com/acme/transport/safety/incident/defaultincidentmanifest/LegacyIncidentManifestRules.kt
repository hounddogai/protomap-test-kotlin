package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.text.normalize

/** A rule a legacy incident manifest must pass. Check returns its problems, or none. */
interface LegacyIncidentManifestRule {
    fun check(item: LegacyIncidentManifest): MutableList<String>
}

data class LegacyIncidentManifestRequiredTitleRule(var message: String) : LegacyIncidentManifestRule {
    override fun check(item: LegacyIncidentManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentManifestAllowedTitleRule(
    var allowed: MutableList<String>,
) : LegacyIncidentManifestRule {
    override fun check(item: LegacyIncidentManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentManifestActiveGuardRule(var limit: Int) : LegacyIncidentManifestRule {
    override fun check(item: LegacyIncidentManifest): MutableList<String> {
        if (item.active && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentManifestTitleLengthRule(var maxLength: Int) : LegacyIncidentManifestRule {
    override fun check(item: LegacyIncidentManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentManifestCodePrefixRule(var prefix: String) : LegacyIncidentManifestRule {
    override fun check(item: LegacyIncidentManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentManifestRequiredCodeRule(var message: String) : LegacyIncidentManifestRule {
    override fun check(item: LegacyIncidentManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy incident manifest. */
data class LegacyIncidentManifestValidator(var rules: MutableList<LegacyIncidentManifestRule>) {
    fun validate(item: LegacyIncidentManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyIncidentManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIncidentManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIncidentManifestValidator(): LegacyIncidentManifestValidator {
    return LegacyIncidentManifestValidator(
        mutableListOf<LegacyIncidentManifestRule>(
            LegacyIncidentManifestRequiredTitleRule("legacy incident manifest needs an entry"),
            LegacyIncidentManifestAllowedTitleRule(mutableListOf<String>("strict")),
            LegacyIncidentManifestActiveGuardRule(1000),
            LegacyIncidentManifestTitleLengthRule(32),
            LegacyIncidentManifestCodePrefixRule("OPS"),
            LegacyIncidentManifestRequiredCodeRule("legacy incident manifest needs this field"),
        ),
    )
}
