package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.text.normalize

/** A rule a manual audit proposal must pass. Check returns its problems, or none. */
interface ManualAuditProposalRule {
    fun check(item: ManualAuditProposal): MutableList<String>
}

data class ManualAuditProposalCodePrefixRule(var prefix: String) : ManualAuditProposalRule {
    override fun check(item: ManualAuditProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAuditProposalAllowedTitleRule(var allowed: MutableList<String>) : ManualAuditProposalRule {
    override fun check(item: ManualAuditProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ManualAuditProposalActiveGuardRule(var limit: Double) : ManualAuditProposalRule {
    override fun check(item: ManualAuditProposal): MutableList<String> {
        if (item.active && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ManualAuditProposalLoadKgRangeRule(var min: Double, var max: Double) : ManualAuditProposalRule {
    override fun check(item: ManualAuditProposal): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualAuditProposalCapacityRangeRule(var min: Int, var max: Int) : ManualAuditProposalRule {
    override fun check(item: ManualAuditProposal): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class ManualAuditProposalTitleLengthRule(var maxLength: Int) : ManualAuditProposalRule {
    override fun check(item: ManualAuditProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual audit proposal. */
data class ManualAuditProposalValidator(var rules: MutableList<ManualAuditProposalRule>) {
    fun validate(item: ManualAuditProposal): MutableList<String> {
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

    fun requireValid(item: ManualAuditProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAuditProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAuditProposalValidator(): ManualAuditProposalValidator {
    return ManualAuditProposalValidator(
        mutableListOf<ManualAuditProposalRule>(
            ManualAuditProposalCodePrefixRule("OPS"),
            ManualAuditProposalAllowedTitleRule(mutableListOf<String>("fallback", "scheduled")),
            ManualAuditProposalActiveGuardRule(1.2),
            ManualAuditProposalLoadKgRangeRule(9.4, 3055.6),
            ManualAuditProposalCapacityRangeRule(255, 3600),
            ManualAuditProposalTitleLengthRule(32),
        ),
    )
}
