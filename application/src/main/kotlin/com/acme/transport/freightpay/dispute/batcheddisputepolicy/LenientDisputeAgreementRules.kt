package com.acme.transport.freightpay.dispute.batcheddisputepolicy

/** A rule a lenient dispute agreement must pass. Check returns its problems, or none. */
interface LenientDisputeAgreementRule {
    fun check(item: LenientDisputeAgreement): MutableList<String>
}

data class LenientDisputeAgreementCodePrefixRule(var prefix: String) : LenientDisputeAgreementRule {
    override fun check(item: LenientDisputeAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeAgreementRequiredCodeRule(var message: String) : LenientDisputeAgreementRule {
    override fun check(item: LenientDisputeAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeAgreementUpdatedAtRangeRule(var min: Int, var max: Int) : LenientDisputeAgreementRule {
    override fun check(item: LenientDisputeAgreement): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LenientDisputeAgreementRequiredNameRule(var message: String) : LenientDisputeAgreementRule {
    override fun check(item: LenientDisputeAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeAgreementCodeLengthRule(var maxLength: Int) : LenientDisputeAgreementRule {
    override fun check(item: LenientDisputeAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dispute agreement. */
data class LenientDisputeAgreementValidator(var rules: MutableList<LenientDisputeAgreementRule>) {
    fun validate(item: LenientDisputeAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDisputeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDisputeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDisputeAgreementValidator(): LenientDisputeAgreementValidator {
    return LenientDisputeAgreementValidator(
        mutableListOf<LenientDisputeAgreementRule>(
            LenientDisputeAgreementCodePrefixRule("Q"),
            LenientDisputeAgreementRequiredCodeRule("lenient dispute agreement needs an entry"),
            LenientDisputeAgreementUpdatedAtRangeRule(32, 80),
            LenientDisputeAgreementRequiredNameRule("lenient dispute agreement needs a value"),
            LenientDisputeAgreementCodeLengthRule(3600),
        ),
    )
}
