package com.acme.transport.customs.hold.strictholdaudit

/** A rule a strict hold audit must pass. Check returns its problems, or none. */
interface StrictHoldAuditRule {
    fun check(item: StrictHoldAudit): MutableList<String>
}

data class StrictHoldAuditNotesLengthRule(var maxLength: Int) : StrictHoldAuditRule {
    override fun check(item: StrictHoldAudit): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictHoldAuditRequiredRegionRule(var message: String) : StrictHoldAuditRule {
    override fun check(item: StrictHoldAudit): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictHoldAuditRegionLengthRule(var maxLength: Int) : StrictHoldAuditRule {
    override fun check(item: StrictHoldAudit): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictHoldAuditRequiredNotesRule(var message: String) : StrictHoldAuditRule {
    override fun check(item: StrictHoldAudit): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictHoldAuditRevisionRangeRule(var min: Int, var max: Int) : StrictHoldAuditRule {
    override fun check(item: StrictHoldAudit): MutableList<String> {
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

data class StrictHoldAuditCodeLengthRule(var maxLength: Int) : StrictHoldAuditRule {
    override fun check(item: StrictHoldAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict hold audit. */
data class StrictHoldAuditValidator(var rules: MutableList<StrictHoldAuditRule>) {
    fun validate(item: StrictHoldAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictHoldAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictHoldAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictHoldAuditValidator(): StrictHoldAuditValidator {
    return StrictHoldAuditValidator(
        mutableListOf<StrictHoldAuditRule>(
            StrictHoldAuditNotesLengthRule(1000),
            StrictHoldAuditRequiredRegionRule("strict hold audit needs this field"),
            StrictHoldAuditRegionLengthRule(64),
            StrictHoldAuditRequiredNotesRule("strict hold audit needs this field"),
            StrictHoldAuditRevisionRangeRule(80, 3600),
            StrictHoldAuditCodeLengthRule(64),
        ),
    )
}
