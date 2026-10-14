package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.text.normalize

/** A rule a manual tire agreement must pass. Check returns its problems, or none. */
interface ManualTireAgreementRule {
    fun check(item: ManualTireAgreement): MutableList<String>
}

data class ManualTireAgreementAllowedLabelRule(var allowed: MutableList<String>) : ManualTireAgreementRule {
    override fun check(item: ManualTireAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ManualTireAgreementRequiredCodeRule(var message: String) : ManualTireAgreementRule {
    override fun check(item: ManualTireAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTireAgreementCodePrefixRule(var prefix: String) : ManualTireAgreementRule {
    override fun check(item: ManualTireAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tire agreement. */
data class ManualTireAgreementValidator(var rules: MutableList<ManualTireAgreementRule>) {
    fun validate(item: ManualTireAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTireAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTireAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTireAgreementValidator(): ManualTireAgreementValidator {
    return ManualTireAgreementValidator(
        mutableListOf<ManualTireAgreementRule>(
            ManualTireAgreementAllowedLabelRule(mutableListOf<String>()),
            ManualTireAgreementRequiredCodeRule("manual tire agreement needs a value"),
            ManualTireAgreementCodePrefixRule("REF"),
        ),
    )
}
