package com.acme.transport.fleetops.inspection.policy

/** A rule a inspection agreement must pass. Check returns its problems, or none. */
interface InspectionAgreementRule {
    fun check(item: InspectionAgreement): MutableList<String>
}

data class InspectionAgreementRequiredDescriptionRule(var message: String) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionAgreementRequiredCodeRule(var message: String) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionAgreementPalletsRangeRule(var min: Int, var max: Int) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class InspectionAgreementLabelLengthRule(var maxLength: Int) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
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

data class InspectionAgreementRequiredLabelRule(var message: String) : InspectionAgreementRule {
    override fun check(item: InspectionAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection agreement. */
data class InspectionAgreementValidator(var rules: MutableList<InspectionAgreementRule>) {
    fun validate(item: InspectionAgreement): MutableList<String> {
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
            InspectionAgreementRequiredDescriptionRule("inspection agreement needs this field"),
            InspectionAgreementRequiredCodeRule("inspection agreement needs a value"),
            InspectionAgreementPalletsRangeRule(120, 1000),
            InspectionAgreementLabelLengthRule(32),
            InspectionAgreementCodePrefixRule("X"),
            InspectionAgreementRequiredLabelRule("inspection agreement needs an entry"),
        ),
    )
}
