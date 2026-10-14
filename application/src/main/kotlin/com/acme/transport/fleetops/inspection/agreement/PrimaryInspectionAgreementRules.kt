package com.acme.transport.fleetops.inspection.agreement

/** A rule a primary inspection agreement must pass. Check returns its problems, or none. */
interface PrimaryInspectionAgreementRule {
    fun check(item: PrimaryInspectionAgreement): MutableList<String>
}

data class PrimaryInspectionAgreementCodeLengthRule(var maxLength: Int) : PrimaryInspectionAgreementRule {
    override fun check(item: PrimaryInspectionAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionAgreementRequiredCodeRule(var message: String) : PrimaryInspectionAgreementRule {
    override fun check(item: PrimaryInspectionAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionAgreementLabelLengthRule(var maxLength: Int) : PrimaryInspectionAgreementRule {
    override fun check(item: PrimaryInspectionAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary inspection agreement. */
data class PrimaryInspectionAgreementValidator(var rules: MutableList<PrimaryInspectionAgreementRule>) {
    fun validate(item: PrimaryInspectionAgreement): MutableList<String> {
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

    fun requireValid(item: PrimaryInspectionAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryInspectionAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryInspectionAgreementValidator(): PrimaryInspectionAgreementValidator {
    return PrimaryInspectionAgreementValidator(
        mutableListOf<PrimaryInspectionAgreementRule>(
            PrimaryInspectionAgreementCodeLengthRule(120),
            PrimaryInspectionAgreementRequiredCodeRule("primary inspection agreement needs a value"),
            PrimaryInspectionAgreementLabelLengthRule(64),
        ),
    )
}
