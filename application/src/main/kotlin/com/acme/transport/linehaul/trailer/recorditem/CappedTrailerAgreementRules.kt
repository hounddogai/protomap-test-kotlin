package com.acme.transport.linehaul.trailer.recorditem

/** A rule a capped trailer agreement must pass. Check returns its problems, or none. */
interface CappedTrailerAgreementRule {
    fun check(item: CappedTrailerAgreement): MutableList<String>
}

data class CappedTrailerAgreementTitleLengthRule(var maxLength: Int) : CappedTrailerAgreementRule {
    override fun check(item: CappedTrailerAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerAgreementApprovedGuardRule(var limit: Double) : CappedTrailerAgreementRule {
    override fun check(item: CappedTrailerAgreement): MutableList<String> {
        if (item.approved && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerAgreementRequiredCodeRule(var message: String) : CappedTrailerAgreementRule {
    override fun check(item: CappedTrailerAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped trailer agreement. */
data class CappedTrailerAgreementValidator(var rules: MutableList<CappedTrailerAgreementRule>) {
    fun validate(item: CappedTrailerAgreement): MutableList<String> {
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

    fun requireValid(item: CappedTrailerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerAgreementValidator(): CappedTrailerAgreementValidator {
    return CappedTrailerAgreementValidator(
        mutableListOf<CappedTrailerAgreementRule>(
            CappedTrailerAgreementTitleLengthRule(86400),
            CappedTrailerAgreementApprovedGuardRule(2.5),
            CappedTrailerAgreementRequiredCodeRule("capped trailer agreement needs an entry"),
        ),
    )
}
