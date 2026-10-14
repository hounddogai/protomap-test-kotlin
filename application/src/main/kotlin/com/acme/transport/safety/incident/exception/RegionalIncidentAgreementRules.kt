package com.acme.transport.safety.incident.exception

import com.acme.transport.kit.text.normalize

/** A rule a regional incident agreement must pass. Check returns its problems, or none. */
interface RegionalIncidentAgreementRule {
    fun check(item: RegionalIncidentAgreement): MutableList<String>
}

data class RegionalIncidentAgreementExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : RegionalIncidentAgreementRule {
    override fun check(item: RegionalIncidentAgreement): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentAgreementChannelLengthRule(var maxLength: Int) : RegionalIncidentAgreementRule {
    override fun check(item: RegionalIncidentAgreement): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentAgreementCodePrefixRule(var prefix: String) : RegionalIncidentAgreementRule {
    override fun check(item: RegionalIncidentAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentAgreementRequiredChannelRule(var message: String) : RegionalIncidentAgreementRule {
    override fun check(item: RegionalIncidentAgreement): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentAgreementAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalIncidentAgreementRule {
    override fun check(item: RegionalIncidentAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentAgreementCodeLengthRule(var maxLength: Int) : RegionalIncidentAgreementRule {
    override fun check(item: RegionalIncidentAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional incident agreement. */
data class RegionalIncidentAgreementValidator(var rules: MutableList<RegionalIncidentAgreementRule>) {
    fun validate(item: RegionalIncidentAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalIncidentAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIncidentAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIncidentAgreementValidator(): RegionalIncidentAgreementValidator {
    return RegionalIncidentAgreementValidator(
        mutableListOf<RegionalIncidentAgreementRule>(
            RegionalIncidentAgreementExpiresAtRangeRule(16, 32),
            RegionalIncidentAgreementChannelLengthRule(3600),
            RegionalIncidentAgreementCodePrefixRule("ACM"),
            RegionalIncidentAgreementRequiredChannelRule("regional incident agreement needs this field"),
            RegionalIncidentAgreementAllowedCodeRule(mutableListOf<String>("batched", "regional", "legacy")),
            RegionalIncidentAgreementCodeLengthRule(16),
        ),
    )
}
