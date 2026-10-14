package com.acme.transport.terminal.dockdoor.window

/** A rule a pending dock door agreement must pass. Check returns its problems, or none. */
interface PendingDockDoorAgreementRule {
    fun check(item: PendingDockDoorAgreement): MutableList<String>
}

data class PendingDockDoorAgreementRequiredLabelRule(var message: String) : PendingDockDoorAgreementRule {
    override fun check(item: PendingDockDoorAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorAgreementRequiredShiftCodeRule(var message: String) : PendingDockDoorAgreementRule {
    override fun check(item: PendingDockDoorAgreement): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorAgreementCodeLengthRule(var maxLength: Int) : PendingDockDoorAgreementRule {
    override fun check(item: PendingDockDoorAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dock door agreement. */
data class PendingDockDoorAgreementValidator(var rules: MutableList<PendingDockDoorAgreementRule>) {
    fun validate(item: PendingDockDoorAgreement): MutableList<String> {
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

    fun requireValid(item: PendingDockDoorAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDockDoorAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDockDoorAgreementValidator(): PendingDockDoorAgreementValidator {
    return PendingDockDoorAgreementValidator(
        mutableListOf<PendingDockDoorAgreementRule>(
            PendingDockDoorAgreementRequiredLabelRule("pending dock door agreement needs a value"),
            PendingDockDoorAgreementRequiredShiftCodeRule("pending dock door agreement needs an entry"),
            PendingDockDoorAgreementCodeLengthRule(64),
        ),
    )
}
