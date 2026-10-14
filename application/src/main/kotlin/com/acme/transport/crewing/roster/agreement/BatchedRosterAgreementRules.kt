package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.text.normalize

/** A rule a batched roster agreement must pass. Check returns its problems, or none. */
interface BatchedRosterAgreementRule {
    fun check(item: BatchedRosterAgreement): MutableList<String>
}

data class BatchedRosterAgreementCodeLengthRule(var maxLength: Int) : BatchedRosterAgreementRule {
    override fun check(item: BatchedRosterAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAgreementTitleLengthRule(var maxLength: Int) : BatchedRosterAgreementRule {
    override fun check(item: BatchedRosterAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAgreementRequiredDriverIdRule(var message: String) : BatchedRosterAgreementRule {
    override fun check(item: BatchedRosterAgreement): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAgreementRequiredTitleRule(var message: String) : BatchedRosterAgreementRule {
    override fun check(item: BatchedRosterAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAgreementAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedRosterAgreementRule {
    override fun check(item: BatchedRosterAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched roster agreement. */
data class BatchedRosterAgreementValidator(var rules: MutableList<BatchedRosterAgreementRule>) {
    fun validate(item: BatchedRosterAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRosterAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRosterAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRosterAgreementValidator(): BatchedRosterAgreementValidator {
    return BatchedRosterAgreementValidator(
        mutableListOf<BatchedRosterAgreementRule>(
            BatchedRosterAgreementCodeLengthRule(120),
            BatchedRosterAgreementTitleLengthRule(32),
            BatchedRosterAgreementRequiredDriverIdRule("batched roster agreement needs an entry"),
            BatchedRosterAgreementRequiredTitleRule("batched roster agreement needs a value"),
            BatchedRosterAgreementAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
