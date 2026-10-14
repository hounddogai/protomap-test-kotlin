package com.acme.transport.terminal.dockdoor.slot

/** A rule a primary dock door audit must pass. Check returns its problems, or none. */
interface PrimaryDockDoorAuditRule {
    fun check(item: PrimaryDockDoorAudit): MutableList<String>
}

data class PrimaryDockDoorAuditCodeLengthRule(var maxLength: Int) : PrimaryDockDoorAuditRule {
    override fun check(item: PrimaryDockDoorAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDockDoorAuditRequiredTrailerIdRule(var message: String) : PrimaryDockDoorAuditRule {
    override fun check(item: PrimaryDockDoorAudit): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDockDoorAuditRequiredChannelRule(var message: String) : PrimaryDockDoorAuditRule {
    override fun check(item: PrimaryDockDoorAudit): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary dock door audit. */
data class PrimaryDockDoorAuditValidator(var rules: MutableList<PrimaryDockDoorAuditRule>) {
    fun validate(item: PrimaryDockDoorAudit): MutableList<String> {
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

    fun requireValid(item: PrimaryDockDoorAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDockDoorAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDockDoorAuditValidator(): PrimaryDockDoorAuditValidator {
    return PrimaryDockDoorAuditValidator(
        mutableListOf<PrimaryDockDoorAuditRule>(
            PrimaryDockDoorAuditCodeLengthRule(64),
            PrimaryDockDoorAuditRequiredTrailerIdRule("primary dock door audit needs this field"),
            PrimaryDockDoorAuditRequiredChannelRule("primary dock door audit needs this field"),
        ),
    )
}
