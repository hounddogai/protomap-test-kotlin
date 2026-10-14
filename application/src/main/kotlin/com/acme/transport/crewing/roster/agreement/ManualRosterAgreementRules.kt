package com.acme.transport.crewing.roster.agreement

/** A rule a manual roster agreement must pass. Check returns its problems, or none. */
interface ManualRosterAgreementRule {
    fun check(item: ManualRosterAgreement): MutableList<String>
}

data class ManualRosterAgreementRequiredLabelRule(var message: String) : ManualRosterAgreementRule {
    override fun check(item: ManualRosterAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRosterAgreementRequiredCodeRule(var message: String) : ManualRosterAgreementRule {
    override fun check(item: ManualRosterAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRosterAgreementCodePrefixRule(var prefix: String) : ManualRosterAgreementRule {
    override fun check(item: ManualRosterAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual roster agreement. */
data class ManualRosterAgreementValidator(var rules: MutableList<ManualRosterAgreementRule>) {
    fun validate(item: ManualRosterAgreement): MutableList<String> {
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

    fun requireValid(item: ManualRosterAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRosterAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRosterAgreementValidator(): ManualRosterAgreementValidator {
    return ManualRosterAgreementValidator(
        mutableListOf<ManualRosterAgreementRule>(
            ManualRosterAgreementRequiredLabelRule("manual roster agreement needs this field"),
            ManualRosterAgreementRequiredCodeRule("manual roster agreement needs an entry"),
            ManualRosterAgreementCodePrefixRule("Q"),
        ),
    )
}
