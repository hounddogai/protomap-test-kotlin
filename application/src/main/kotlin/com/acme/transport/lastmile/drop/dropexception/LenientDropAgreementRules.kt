package com.acme.transport.lastmile.drop.dropexception

/** A rule a lenient drop agreement must pass. Check returns its problems, or none. */
interface LenientDropAgreementRule {
    fun check(item: LenientDropAgreement): MutableList<String>
}

data class LenientDropAgreementRequiredSummaryRule(var message: String) : LenientDropAgreementRule {
    override fun check(item: LenientDropAgreement): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDropAgreementSummaryLengthRule(var maxLength: Int) : LenientDropAgreementRule {
    override fun check(item: LenientDropAgreement): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropAgreementRequiredUnitRule(var message: String) : LenientDropAgreementRule {
    override fun check(item: LenientDropAgreement): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient drop agreement. */
data class LenientDropAgreementValidator(var rules: MutableList<LenientDropAgreementRule>) {
    fun validate(item: LenientDropAgreement): MutableList<String> {
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

    fun requireValid(item: LenientDropAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDropAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDropAgreementValidator(): LenientDropAgreementValidator {
    return LenientDropAgreementValidator(
        mutableListOf<LenientDropAgreementRule>(
            LenientDropAgreementRequiredSummaryRule("lenient drop agreement needs a value"),
            LenientDropAgreementSummaryLengthRule(255),
            LenientDropAgreementRequiredUnitRule("lenient drop agreement needs this field"),
        ),
    )
}
