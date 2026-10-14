package com.acme.transport.network.node.agreement

import com.acme.transport.kit.text.normalize

/** A rule a seasonal node agreement must pass. Check returns its problems, or none. */
interface SeasonalNodeAgreementRule {
    fun check(item: SeasonalNodeAgreement): MutableList<String>
}

data class SeasonalNodeAgreementDriverIdLengthRule(var maxLength: Int) : SeasonalNodeAgreementRule {
    override fun check(item: SeasonalNodeAgreement): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodeAgreementCodePrefixRule(var prefix: String) : SeasonalNodeAgreementRule {
    override fun check(item: SeasonalNodeAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodeAgreementLabelLengthRule(var maxLength: Int) : SeasonalNodeAgreementRule {
    override fun check(item: SeasonalNodeAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodeAgreementAllowedCodeRule(var allowed: MutableList<String>) : SeasonalNodeAgreementRule {
    override fun check(item: SeasonalNodeAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal node agreement. */
data class SeasonalNodeAgreementValidator(var rules: MutableList<SeasonalNodeAgreementRule>) {
    fun validate(item: SeasonalNodeAgreement): MutableList<String> {
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

    fun requireValid(item: SeasonalNodeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalNodeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalNodeAgreementValidator(): SeasonalNodeAgreementValidator {
    return SeasonalNodeAgreementValidator(
        mutableListOf<SeasonalNodeAgreementRule>(
            SeasonalNodeAgreementDriverIdLengthRule(64),
            SeasonalNodeAgreementCodePrefixRule("ACM"),
            SeasonalNodeAgreementLabelLengthRule(32),
            SeasonalNodeAgreementAllowedCodeRule(mutableListOf<String>("strict")),
        ),
    )
}
