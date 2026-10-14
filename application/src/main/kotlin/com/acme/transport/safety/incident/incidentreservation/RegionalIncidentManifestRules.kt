package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.text.normalize

/** A rule a regional incident manifest must pass. Check returns its problems, or none. */
interface RegionalIncidentManifestRule {
    fun check(item: RegionalIncidentManifest): MutableList<String>
}

data class RegionalIncidentManifestRequiredDriverIdRule(var message: String) : RegionalIncidentManifestRule {
    override fun check(item: RegionalIncidentManifest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentManifestCodeLengthRule(var maxLength: Int) : RegionalIncidentManifestRule {
    override fun check(item: RegionalIncidentManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentManifestAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalIncidentManifestRule {
    override fun check(item: RegionalIncidentManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentManifestLimitRangeRule(var min: Int, var max: Int) : RegionalIncidentManifestRule {
    override fun check(item: RegionalIncidentManifest): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class RegionalIncidentManifestCodePrefixRule(var prefix: String) : RegionalIncidentManifestRule {
    override fun check(item: RegionalIncidentManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional incident manifest. */
data class RegionalIncidentManifestValidator(var rules: MutableList<RegionalIncidentManifestRule>) {
    fun validate(item: RegionalIncidentManifest): MutableList<String> {
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

    fun requireValid(item: RegionalIncidentManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIncidentManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIncidentManifestValidator(): RegionalIncidentManifestValidator {
    return RegionalIncidentManifestValidator(
        mutableListOf<RegionalIncidentManifestRule>(
            RegionalIncidentManifestRequiredDriverIdRule("regional incident manifest needs this field"),
            RegionalIncidentManifestCodeLengthRule(32),
            RegionalIncidentManifestAllowedCodeRule(mutableListOf<String>("rolling", "adaptive", "global")),
            RegionalIncidentManifestLimitRangeRule(16, 3600),
            RegionalIncidentManifestCodePrefixRule("Q"),
        ),
    )
}
