package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.text.normalize

/** A rule a manual incident manifest must pass. Check returns its problems, or none. */
interface ManualIncidentManifestRule {
    fun check(item: ManualIncidentManifest): MutableList<String>
}

data class ManualIncidentManifestRequiredSummaryRule(var message: String) : ManualIncidentManifestRule {
    override fun check(item: ManualIncidentManifest): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentManifestAllowedNameRule(
    var allowed: MutableList<String>,
) : ManualIncidentManifestRule {
    override fun check(item: ManualIncidentManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentManifestWeightRangeRule(
    var min: Double,
    var max: Double,
) : ManualIncidentManifestRule {
    override fun check(item: ManualIncidentManifest): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentManifestRequiredCodeRule(var message: String) : ManualIncidentManifestRule {
    override fun check(item: ManualIncidentManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentManifestCodeLengthRule(var maxLength: Int) : ManualIncidentManifestRule {
    override fun check(item: ManualIncidentManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual incident manifest. */
data class ManualIncidentManifestValidator(var rules: MutableList<ManualIncidentManifestRule>) {
    fun validate(item: ManualIncidentManifest): MutableList<String> {
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

    fun requireValid(item: ManualIncidentManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIncidentManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIncidentManifestValidator(): ManualIncidentManifestValidator {
    return ManualIncidentManifestValidator(
        mutableListOf<ManualIncidentManifestRule>(
            ManualIncidentManifestRequiredSummaryRule("manual incident manifest needs an entry"),
            ManualIncidentManifestAllowedNameRule(mutableListOf<String>("default", "global", "manual")),
            ManualIncidentManifestWeightRangeRule(4.4, 2708.4),
            ManualIncidentManifestRequiredCodeRule("manual incident manifest needs a value"),
            ManualIncidentManifestCodeLengthRule(1000),
        ),
    )
}
