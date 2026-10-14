package com.acme.transport.fleetops.inspection.inspectionagreement

/** A rule a capped inspection agreement must pass. Check returns its problems, or none. */
interface CappedInspectionAgreementRule {
    fun check(item: CappedInspectionAgreement): MutableList<String>
}

data class CappedInspectionAgreementOdometerRangeRule(
    var min: Int,
    var max: Int,
) : CappedInspectionAgreementRule {
    override fun check(item: CappedInspectionAgreement): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionAgreementRequiredCodeRule(var message: String) : CappedInspectionAgreementRule {
    override fun check(item: CappedInspectionAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionAgreementCountryLengthRule(var maxLength: Int) : CappedInspectionAgreementRule {
    override fun check(item: CappedInspectionAgreement): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionAgreementCodeLengthRule(var maxLength: Int) : CappedInspectionAgreementRule {
    override fun check(item: CappedInspectionAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionAgreementRequiredCountryRule(var message: String) : CappedInspectionAgreementRule {
    override fun check(item: CappedInspectionAgreement): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionAgreementRequiredNameRule(var message: String) : CappedInspectionAgreementRule {
    override fun check(item: CappedInspectionAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped inspection agreement. */
data class CappedInspectionAgreementValidator(var rules: MutableList<CappedInspectionAgreementRule>) {
    fun validate(item: CappedInspectionAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedInspectionAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedInspectionAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedInspectionAgreementValidator(): CappedInspectionAgreementValidator {
    return CappedInspectionAgreementValidator(
        mutableListOf<CappedInspectionAgreementRule>(
            CappedInspectionAgreementOdometerRangeRule(255, 3600),
            CappedInspectionAgreementRequiredCodeRule("capped inspection agreement needs a value"),
            CappedInspectionAgreementCountryLengthRule(3600),
            CappedInspectionAgreementCodeLengthRule(16),
            CappedInspectionAgreementRequiredCountryRule("capped inspection agreement needs a value"),
            CappedInspectionAgreementRequiredNameRule("capped inspection agreement needs this field"),
        ),
    )
}
