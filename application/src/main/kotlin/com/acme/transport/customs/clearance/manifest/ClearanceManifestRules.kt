package com.acme.transport.customs.clearance.manifest

import com.acme.transport.kit.text.normalize

/** A rule a clearance manifest must pass. Check returns its problems, or none. */
interface ClearanceManifestRule {
    fun check(item: ClearanceManifest): MutableList<String>
}

data class ClearanceManifestNameLengthRule(var maxLength: Int) : ClearanceManifestRule {
    override fun check(item: ClearanceManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceManifestCodeLengthRule(var maxLength: Int) : ClearanceManifestRule {
    override fun check(item: ClearanceManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceManifestExpiresAtRangeRule(var min: Int, var max: Int) : ClearanceManifestRule {
    override fun check(item: ClearanceManifest): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class ClearanceManifestCodePrefixRule(var prefix: String) : ClearanceManifestRule {
    override fun check(item: ClearanceManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceManifestAllowedPhoneRule(var allowed: MutableList<String>) : ClearanceManifestRule {
    override fun check(item: ClearanceManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance manifest. */
data class ClearanceManifestValidator(var rules: MutableList<ClearanceManifestRule>) {
    fun validate(item: ClearanceManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceManifestValidator(): ClearanceManifestValidator {
    return ClearanceManifestValidator(
        mutableListOf<ClearanceManifestRule>(
            ClearanceManifestNameLengthRule(1000),
            ClearanceManifestCodeLengthRule(16),
            ClearanceManifestExpiresAtRangeRule(255, 86400),
            ClearanceManifestCodePrefixRule("Q"),
            ClearanceManifestAllowedPhoneRule(mutableListOf<String>("regional", "legacy", "strict")),
        ),
    )
}
