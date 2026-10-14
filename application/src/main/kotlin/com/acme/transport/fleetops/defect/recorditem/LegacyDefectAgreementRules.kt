package com.acme.transport.fleetops.defect.recorditem

/** A rule a legacy defect agreement must pass. Check returns its problems, or none. */
interface LegacyDefectAgreementRule {
    fun check(item: LegacyDefectAgreement): MutableList<String>
}

data class LegacyDefectAgreementRequiredLocaleRule(var message: String) : LegacyDefectAgreementRule {
    override fun check(item: LegacyDefectAgreement): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectAgreementOdometerRangeRule(var min: Int, var max: Int) : LegacyDefectAgreementRule {
    override fun check(item: LegacyDefectAgreement): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class LegacyDefectAgreementRequiredCurrencyRule(var message: String) : LegacyDefectAgreementRule {
    override fun check(item: LegacyDefectAgreement): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectAgreementCodeLengthRule(var maxLength: Int) : LegacyDefectAgreementRule {
    override fun check(item: LegacyDefectAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectAgreementRequiredNameRule(var message: String) : LegacyDefectAgreementRule {
    override fun check(item: LegacyDefectAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy defect agreement. */
data class LegacyDefectAgreementValidator(var rules: MutableList<LegacyDefectAgreementRule>) {
    fun validate(item: LegacyDefectAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDefectAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDefectAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDefectAgreementValidator(): LegacyDefectAgreementValidator {
    return LegacyDefectAgreementValidator(
        mutableListOf<LegacyDefectAgreementRule>(
            LegacyDefectAgreementRequiredLocaleRule("legacy defect agreement needs an entry"),
            LegacyDefectAgreementOdometerRangeRule(80, 1000),
            LegacyDefectAgreementRequiredCurrencyRule("legacy defect agreement needs a value"),
            LegacyDefectAgreementCodeLengthRule(1000),
            LegacyDefectAgreementRequiredNameRule("legacy defect agreement needs an entry"),
        ),
    )
}
