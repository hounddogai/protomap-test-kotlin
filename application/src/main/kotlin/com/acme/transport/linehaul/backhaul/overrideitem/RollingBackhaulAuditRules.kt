package com.acme.transport.linehaul.backhaul.overrideitem

/** A rule a rolling backhaul audit must pass. Check returns its problems, or none. */
interface RollingBackhaulAuditRule {
    fun check(item: RollingBackhaulAudit): MutableList<String>
}

data class RollingBackhaulAuditLaneLengthRule(var maxLength: Int) : RollingBackhaulAuditRule {
    override fun check(item: RollingBackhaulAudit): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulAuditRequiredLabelRule(var message: String) : RollingBackhaulAuditRule {
    override fun check(item: RollingBackhaulAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulAuditRequiredRegionRule(var message: String) : RollingBackhaulAuditRule {
    override fun check(item: RollingBackhaulAudit): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulAuditMaximumRangeRule(var min: Double, var max: Double) : RollingBackhaulAuditRule {
    override fun check(item: RollingBackhaulAudit): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling backhaul audit. */
data class RollingBackhaulAuditValidator(var rules: MutableList<RollingBackhaulAuditRule>) {
    fun validate(item: RollingBackhaulAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingBackhaulAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingBackhaulAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingBackhaulAuditValidator(): RollingBackhaulAuditValidator {
    return RollingBackhaulAuditValidator(
        mutableListOf<RollingBackhaulAuditRule>(
            RollingBackhaulAuditLaneLengthRule(255),
            RollingBackhaulAuditRequiredLabelRule("rolling backhaul audit needs this field"),
            RollingBackhaulAuditRequiredRegionRule("rolling backhaul audit needs an entry"),
            RollingBackhaulAuditMaximumRangeRule(3.5, 3531.7),
        ),
    )
}
