package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a primary ev charge digest must pass. Check returns its problems, or none. */
interface PrimaryEvChargeDigestRule {
    fun check(item: PrimaryEvChargeDigest): MutableList<String>
}

data class PrimaryEvChargeDigestRequiredUnitRule(var message: String) : PrimaryEvChargeDigestRule {
    override fun check(item: PrimaryEvChargeDigest): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeDigestAllowedCodeRule(var allowed: MutableList<String>) : PrimaryEvChargeDigestRule {
    override fun check(item: PrimaryEvChargeDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeDigestVersionRangeRule(var min: Int, var max: Int) : PrimaryEvChargeDigestRule {
    override fun check(item: PrimaryEvChargeDigest): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class PrimaryEvChargeDigestCodePrefixRule(var prefix: String) : PrimaryEvChargeDigestRule {
    override fun check(item: PrimaryEvChargeDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary ev charge digest. */
data class PrimaryEvChargeDigestValidator(var rules: MutableList<PrimaryEvChargeDigestRule>) {
    fun validate(item: PrimaryEvChargeDigest): MutableList<String> {
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

    fun requireValid(item: PrimaryEvChargeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryEvChargeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryEvChargeDigestValidator(): PrimaryEvChargeDigestValidator {
    return PrimaryEvChargeDigestValidator(
        mutableListOf<PrimaryEvChargeDigestRule>(
            PrimaryEvChargeDigestRequiredUnitRule("primary ev charge digest needs a value"),
            PrimaryEvChargeDigestAllowedCodeRule(mutableListOf<String>("capped", "regional")),
            PrimaryEvChargeDigestVersionRangeRule(255, 1000),
            PrimaryEvChargeDigestCodePrefixRule("REF"),
        ),
    )
}
