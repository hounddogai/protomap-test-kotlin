package com.acme.transport.crewing.qualification.rollup

/** A rule a primary qualification quota must pass. Check returns its problems, or none. */
interface PrimaryQualificationQuotaRule {
    fun check(item: PrimaryQualificationQuota): MutableList<String>
}

data class PrimaryQualificationQuotaCurrencyLengthRule(var maxLength: Int) : PrimaryQualificationQuotaRule {
    override fun check(item: PrimaryQualificationQuota): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationQuotaRequiredCodeRule(var message: String) : PrimaryQualificationQuotaRule {
    override fun check(item: PrimaryQualificationQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationQuotaCodeLengthRule(var maxLength: Int) : PrimaryQualificationQuotaRule {
    override fun check(item: PrimaryQualificationQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationQuotaRequiredCurrencyRule(var message: String) : PrimaryQualificationQuotaRule {
    override fun check(item: PrimaryQualificationQuota): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationQuotaSequenceRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryQualificationQuotaRule {
    override fun check(item: PrimaryQualificationQuota): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary qualification quota. */
data class PrimaryQualificationQuotaValidator(var rules: MutableList<PrimaryQualificationQuotaRule>) {
    fun validate(item: PrimaryQualificationQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryQualificationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryQualificationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryQualificationQuotaValidator(): PrimaryQualificationQuotaValidator {
    return PrimaryQualificationQuotaValidator(
        mutableListOf<PrimaryQualificationQuotaRule>(
            PrimaryQualificationQuotaCurrencyLengthRule(255),
            PrimaryQualificationQuotaRequiredCodeRule("primary qualification quota needs this field"),
            PrimaryQualificationQuotaCodeLengthRule(3600),
            PrimaryQualificationQuotaRequiredCurrencyRule("primary qualification quota needs an entry"),
            PrimaryQualificationQuotaSequenceRangeRule(64, 3600),
        ),
    )
}
