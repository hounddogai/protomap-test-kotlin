package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.normalize

/** A rule a capped node agreement must pass. Check returns its problems, or none. */
interface CappedNodeAgreementRule {
    fun check(item: CappedNodeAgreement): MutableList<String>
}

data class CappedNodeAgreementCodeLengthRule(var maxLength: Int) : CappedNodeAgreementRule {
    override fun check(item: CappedNodeAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeAgreementRequiredCodeRule(var message: String) : CappedNodeAgreementRule {
    override fun check(item: CappedNodeAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeAgreementAllowedCodeRule(var allowed: MutableList<String>) : CappedNodeAgreementRule {
    override fun check(item: CappedNodeAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeAgreementRequiredTrailerIdRule(var message: String) : CappedNodeAgreementRule {
    override fun check(item: CappedNodeAgreement): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeAgreementRequiredTitleRule(var message: String) : CappedNodeAgreementRule {
    override fun check(item: CappedNodeAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeAgreementGraceDaysRangeRule(var min: Int, var max: Int) : CappedNodeAgreementRule {
    override fun check(item: CappedNodeAgreement): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped node agreement. */
data class CappedNodeAgreementValidator(var rules: MutableList<CappedNodeAgreementRule>) {
    fun validate(item: CappedNodeAgreement): MutableList<String> {
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

    fun requireValid(item: CappedNodeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedNodeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedNodeAgreementValidator(): CappedNodeAgreementValidator {
    return CappedNodeAgreementValidator(
        mutableListOf<CappedNodeAgreementRule>(
            CappedNodeAgreementCodeLengthRule(255),
            CappedNodeAgreementRequiredCodeRule("capped node agreement needs this field"),
            CappedNodeAgreementAllowedCodeRule(mutableListOf<String>()),
            CappedNodeAgreementRequiredTrailerIdRule("capped node agreement needs an entry"),
            CappedNodeAgreementRequiredTitleRule("capped node agreement needs a value"),
            CappedNodeAgreementGraceDaysRangeRule(16, 3600),
        ),
    )
}
