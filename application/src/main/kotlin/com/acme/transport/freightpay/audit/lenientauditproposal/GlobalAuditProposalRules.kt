package com.acme.transport.freightpay.audit.lenientauditproposal

/** A rule a global audit proposal must pass. Check returns its problems, or none. */
interface GlobalAuditProposalRule {
    fun check(item: GlobalAuditProposal): MutableList<String>
}

data class GlobalAuditProposalLocaleLengthRule(var maxLength: Int) : GlobalAuditProposalRule {
    override fun check(item: GlobalAuditProposal): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditProposalCodeLengthRule(var maxLength: Int) : GlobalAuditProposalRule {
    override fun check(item: GlobalAuditProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditProposalRequiredTitleRule(var message: String) : GlobalAuditProposalRule {
    override fun check(item: GlobalAuditProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditProposalCreatedAtRangeRule(var min: Int, var max: Int) : GlobalAuditProposalRule {
    override fun check(item: GlobalAuditProposal): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditProposalRequiredLocaleRule(var message: String) : GlobalAuditProposalRule {
    override fun check(item: GlobalAuditProposal): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditProposalAttemptsRangeRule(var min: Int, var max: Int) : GlobalAuditProposalRule {
    override fun check(item: GlobalAuditProposal): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global audit proposal. */
data class GlobalAuditProposalValidator(var rules: MutableList<GlobalAuditProposalRule>) {
    fun validate(item: GlobalAuditProposal): MutableList<String> {
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

    fun requireValid(item: GlobalAuditProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAuditProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAuditProposalValidator(): GlobalAuditProposalValidator {
    return GlobalAuditProposalValidator(
        mutableListOf<GlobalAuditProposalRule>(
            GlobalAuditProposalLocaleLengthRule(64),
            GlobalAuditProposalCodeLengthRule(80),
            GlobalAuditProposalRequiredTitleRule("global audit proposal needs this field"),
            GlobalAuditProposalCreatedAtRangeRule(16, 64),
            GlobalAuditProposalRequiredLocaleRule("global audit proposal needs an entry"),
            GlobalAuditProposalAttemptsRangeRule(32, 80),
        ),
    )
}
