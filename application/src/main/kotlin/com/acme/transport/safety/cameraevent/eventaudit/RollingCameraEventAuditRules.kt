package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.normalize

/** A rule a rolling camera event audit must pass. Check returns its problems, or none. */
interface RollingCameraEventAuditRule {
    fun check(item: RollingCameraEventAudit): MutableList<String>
}

data class RollingCameraEventAuditAllowedLabelRule(
    var allowed: MutableList<String>,
) : RollingCameraEventAuditRule {
    override fun check(item: RollingCameraEventAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RollingCameraEventAuditRequiredCodeRule(var message: String) : RollingCameraEventAuditRule {
    override fun check(item: RollingCameraEventAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCameraEventAuditRequiredDepotRule(var message: String) : RollingCameraEventAuditRule {
    override fun check(item: RollingCameraEventAudit): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling camera event audit. */
data class RollingCameraEventAuditValidator(var rules: MutableList<RollingCameraEventAuditRule>) {
    fun validate(item: RollingCameraEventAudit): MutableList<String> {
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

    fun requireValid(item: RollingCameraEventAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCameraEventAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCameraEventAuditValidator(): RollingCameraEventAuditValidator {
    return RollingCameraEventAuditValidator(
        mutableListOf<RollingCameraEventAuditRule>(
            RollingCameraEventAuditAllowedLabelRule(mutableListOf<String>("weighted")),
            RollingCameraEventAuditRequiredCodeRule("rolling camera event audit needs an entry"),
            RollingCameraEventAuditRequiredDepotRule("rolling camera event audit needs a value"),
        ),
    )
}
