package com.acme.transport.linehaul.laneplan.schedule

/** A rule a lane plan audit must pass. Check returns its problems, or none. */
interface LanePlanAuditRule {
    fun check(item: LanePlanAudit): MutableList<String>
}

data class LanePlanAuditVersionRangeRule(var min: Int, var max: Int) : LanePlanAuditRule {
    override fun check(item: LanePlanAudit): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanAuditRequiredSummaryRule(var message: String) : LanePlanAuditRule {
    override fun check(item: LanePlanAudit): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanAuditRevisionRangeRule(var min: Int, var max: Int) : LanePlanAuditRule {
    override fun check(item: LanePlanAudit): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lane plan audit. */
data class LanePlanAuditValidator(var rules: MutableList<LanePlanAuditRule>) {
    fun validate(item: LanePlanAudit): MutableList<String> {
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

    fun requireValid(item: LanePlanAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanAuditValidator(): LanePlanAuditValidator {
    return LanePlanAuditValidator(
        mutableListOf<LanePlanAuditRule>(
            LanePlanAuditVersionRangeRule(80, 120),
            LanePlanAuditRequiredSummaryRule("lane plan audit needs an entry"),
            LanePlanAuditRevisionRangeRule(64, 3600),
        ),
    )
}
