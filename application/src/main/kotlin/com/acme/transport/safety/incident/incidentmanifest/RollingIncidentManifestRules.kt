package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.normalize

/** A rule a rolling incident manifest must pass. Check returns its problems, or none. */
interface RollingIncidentManifestRule {
    fun check(item: RollingIncidentManifest): MutableList<String>
}

data class RollingIncidentManifestOdometerRangeRule(var min: Int, var max: Int) : RollingIncidentManifestRule {
    override fun check(item: RollingIncidentManifest): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class RollingIncidentManifestNotesLengthRule(var maxLength: Int) : RollingIncidentManifestRule {
    override fun check(item: RollingIncidentManifest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentManifestAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingIncidentManifestRule {
    override fun check(item: RollingIncidentManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentManifestRequiredCodeRule(var message: String) : RollingIncidentManifestRule {
    override fun check(item: RollingIncidentManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentManifestStopCountRangeRule(var min: Int, var max: Int) : RollingIncidentManifestRule {
    override fun check(item: RollingIncidentManifest): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class RollingIncidentManifestTitleLengthRule(var maxLength: Int) : RollingIncidentManifestRule {
    override fun check(item: RollingIncidentManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling incident manifest. */
data class RollingIncidentManifestValidator(var rules: MutableList<RollingIncidentManifestRule>) {
    fun validate(item: RollingIncidentManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingIncidentManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingIncidentManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingIncidentManifestValidator(): RollingIncidentManifestValidator {
    return RollingIncidentManifestValidator(
        mutableListOf<RollingIncidentManifestRule>(
            RollingIncidentManifestOdometerRangeRule(80, 255),
            RollingIncidentManifestNotesLengthRule(64),
            RollingIncidentManifestAllowedCodeRule(mutableListOf<String>("weighted", "batched")),
            RollingIncidentManifestRequiredCodeRule("rolling incident manifest needs this field"),
            RollingIncidentManifestStopCountRangeRule(16, 80),
            RollingIncidentManifestTitleLengthRule(16),
        ),
    )
}
