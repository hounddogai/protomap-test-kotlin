package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.text.normalize

/** A rule a inspection agreement must pass. Check returns its problems, or none. */
interface InspectionAgreementRule {
    fun check(item: InspectionAgreement): MutableList<String>
}

data class InspectionAgreementRequiredCodeRule(var message: String) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionAgreementCodePrefixRule(var prefix: String) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAgreementRequiredUnitRule(var message: String) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionAgreementAllowedLabelRule(var allowed: MutableList<String>) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAgreementCodeLengthRule(var maxLength: Int) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection agreement. */
data class InspectionAgreementValidator(var rules: MutableList<InspectionAgreementRule>) {
    fun validate(item: InspectionAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionAgreementValidator(): InspectionAgreementValidator {
    return InspectionAgreementValidator(
        mutableListOf<InspectionAgreementRule>(
            InspectionAgreementRequiredCodeRule("inspection agreement needs a value"),
            InspectionAgreementCodePrefixRule("ACM"),
            InspectionAgreementRequiredUnitRule("inspection agreement needs this field"),
            InspectionAgreementAllowedLabelRule(mutableListOf<String>("lenient", "global")),
            InspectionAgreementCodeLengthRule(16),
        ),
    )
}
