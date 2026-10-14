package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.normalize

/** A rule a adaptive dispute manifest must pass. Check returns its problems, or none. */
interface AdaptiveDisputeManifestRule {
    fun check(item: AdaptiveDisputeManifest): MutableList<String>
}

data class AdaptiveDisputeManifestCodeLengthRule(var maxLength: Int) : AdaptiveDisputeManifestRule {
    override fun check(item: AdaptiveDisputeManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDisputeManifestAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveDisputeManifestRule {
    override fun check(item: AdaptiveDisputeManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDisputeManifestRequiredTimezoneRule(var message: String) : AdaptiveDisputeManifestRule {
    override fun check(item: AdaptiveDisputeManifest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive dispute manifest. */
data class AdaptiveDisputeManifestValidator(var rules: MutableList<AdaptiveDisputeManifestRule>) {
    fun validate(item: AdaptiveDisputeManifest): MutableList<String> {
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

    fun requireValid(item: AdaptiveDisputeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDisputeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDisputeManifestValidator(): AdaptiveDisputeManifestValidator {
    return AdaptiveDisputeManifestValidator(
        mutableListOf<AdaptiveDisputeManifestRule>(
            AdaptiveDisputeManifestCodeLengthRule(16),
            AdaptiveDisputeManifestAllowedCodeRule(mutableListOf<String>()),
            AdaptiveDisputeManifestRequiredTimezoneRule("adaptive dispute manifest needs this field"),
        ),
    )
}
