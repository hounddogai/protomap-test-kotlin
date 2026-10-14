package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.kit.text.normalize

/** A rule a regional incident snapshot must pass. Check returns its problems, or none. */
interface RegionalIncidentSnapshotRule {
    fun check(item: RegionalIncidentSnapshot): MutableList<String>
}

data class RegionalIncidentSnapshotCodePrefixRule(var prefix: String) : RegionalIncidentSnapshotRule {
    override fun check(item: RegionalIncidentSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentSnapshotRequiredCodeRule(var message: String) : RegionalIncidentSnapshotRule {
    override fun check(item: RegionalIncidentSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentSnapshotRequiredCarrierRule(var message: String) : RegionalIncidentSnapshotRule {
    override fun check(item: RegionalIncidentSnapshot): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentSnapshotAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalIncidentSnapshotRule {
    override fun check(item: RegionalIncidentSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional incident snapshot. */
data class RegionalIncidentSnapshotValidator(var rules: MutableList<RegionalIncidentSnapshotRule>) {
    fun validate(item: RegionalIncidentSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalIncidentSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIncidentSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIncidentSnapshotValidator(): RegionalIncidentSnapshotValidator {
    return RegionalIncidentSnapshotValidator(
        mutableListOf<RegionalIncidentSnapshotRule>(
            RegionalIncidentSnapshotCodePrefixRule("ACM"),
            RegionalIncidentSnapshotRequiredCodeRule("regional incident snapshot needs this field"),
            RegionalIncidentSnapshotRequiredCarrierRule("regional incident snapshot needs a value"),
            RegionalIncidentSnapshotAllowedCodeRule(mutableListOf<String>("pending", "seasonal", "manual")),
        ),
    )
}
