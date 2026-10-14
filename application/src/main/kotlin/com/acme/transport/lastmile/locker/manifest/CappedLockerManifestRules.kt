package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.text.normalize

/** A rule a capped locker manifest must pass. Check returns its problems, or none. */
interface CappedLockerManifestRule {
    fun check(item: CappedLockerManifest): MutableList<String>
}

data class CappedLockerManifestCodePrefixRule(var prefix: String) : CappedLockerManifestRule {
    override fun check(item: CappedLockerManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerManifestAllowedCodeRule(var allowed: MutableList<String>) : CappedLockerManifestRule {
    override fun check(item: CappedLockerManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerManifestWeightRangeRule(var min: Double, var max: Double) : CappedLockerManifestRule {
    override fun check(item: CappedLockerManifest): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped locker manifest. */
data class CappedLockerManifestValidator(var rules: MutableList<CappedLockerManifestRule>) {
    fun validate(item: CappedLockerManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedLockerManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLockerManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLockerManifestValidator(): CappedLockerManifestValidator {
    return CappedLockerManifestValidator(
        mutableListOf<CappedLockerManifestRule>(
            CappedLockerManifestCodePrefixRule("Q"),
            CappedLockerManifestAllowedCodeRule(mutableListOf<String>("strict")),
            CappedLockerManifestWeightRangeRule(7.4, 4347.7),
        ),
    )
}
