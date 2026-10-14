package com.acme.transport.network.hub.agreement

/** A rule a hub agreement must pass. Check returns its problems, or none. */
interface HubAgreementRule {
    fun check(item: HubAgreement): MutableList<String>
}

data class HubAgreementLabelLengthRule(var maxLength: Int) : HubAgreementRule {
    override fun check(item: HubAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubAgreementRequiredLabelRule(var message: String) : HubAgreementRule {
    override fun check(item: HubAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubAgreementCodeLengthRule(var maxLength: Int) : HubAgreementRule {
    override fun check(item: HubAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub agreement. */
data class HubAgreementValidator(var rules: MutableList<HubAgreementRule>) {
    fun validate(item: HubAgreement): MutableList<String> {
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

    fun requireValid(item: HubAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultHubAgreementValidator(): HubAgreementValidator {
    return HubAgreementValidator(
        mutableListOf<HubAgreementRule>(
            HubAgreementLabelLengthRule(64),
            HubAgreementRequiredLabelRule("hub agreement needs a value"),
            HubAgreementCodeLengthRule(86400),
        ),
    )
}
