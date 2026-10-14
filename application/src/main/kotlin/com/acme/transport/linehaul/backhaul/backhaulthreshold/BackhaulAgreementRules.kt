package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.normalize

/** A rule a backhaul agreement must pass. Check returns its problems, or none. */
interface BackhaulAgreementRule {
    fun check(item: BackhaulAgreement): MutableList<String>
}

data class BackhaulAgreementCodePrefixRule(var prefix: String) : BackhaulAgreementRule {
    override fun check(item: BackhaulAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAgreementReviewerLengthRule(var maxLength: Int) : BackhaulAgreementRule {
    override fun check(item: BackhaulAgreement): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAgreementAllowedExternalRefRule(var allowed: MutableList<String>) : BackhaulAgreementRule {
    override fun check(item: BackhaulAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul agreement. */
data class BackhaulAgreementValidator(var rules: MutableList<BackhaulAgreementRule>) {
    fun validate(item: BackhaulAgreement): MutableList<String> {
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

    fun requireValid(item: BackhaulAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulAgreementValidator(): BackhaulAgreementValidator {
    return BackhaulAgreementValidator(
        mutableListOf<BackhaulAgreementRule>(
            BackhaulAgreementCodePrefixRule("X"),
            BackhaulAgreementReviewerLengthRule(64),
            BackhaulAgreementAllowedExternalRefRule(mutableListOf<String>("tiered", "weighted", "seasonal")),
        ),
    )
}
