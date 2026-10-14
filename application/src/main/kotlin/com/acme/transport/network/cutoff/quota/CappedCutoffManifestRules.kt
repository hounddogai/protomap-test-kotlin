package com.acme.transport.network.cutoff.quota

import com.acme.transport.kit.text.normalize

/** A rule a capped cutoff manifest must pass. Check returns its problems, or none. */
interface CappedCutoffManifestRule {
    fun check(item: CappedCutoffManifest): MutableList<String>
}

data class CappedCutoffManifestLimitRangeRule(var min: Int, var max: Int) : CappedCutoffManifestRule {
    override fun check(item: CappedCutoffManifest): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class CappedCutoffManifestAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : CappedCutoffManifestRule {
    override fun check(item: CappedCutoffManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffManifestDriverIdLengthRule(var maxLength: Int) : CappedCutoffManifestRule {
    override fun check(item: CappedCutoffManifest): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffManifestLoadKgRangeRule(var min: Double, var max: Double) : CappedCutoffManifestRule {
    override fun check(item: CappedCutoffManifest): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class CappedCutoffManifestRequiredCodeRule(var message: String) : CappedCutoffManifestRule {
    override fun check(item: CappedCutoffManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffManifestRequiredOwnerIdRule(var message: String) : CappedCutoffManifestRule {
    override fun check(item: CappedCutoffManifest): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped cutoff manifest. */
data class CappedCutoffManifestValidator(var rules: MutableList<CappedCutoffManifestRule>) {
    fun validate(item: CappedCutoffManifest): MutableList<String> {
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

    fun requireValid(item: CappedCutoffManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCutoffManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCutoffManifestValidator(): CappedCutoffManifestValidator {
    return CappedCutoffManifestValidator(
        mutableListOf<CappedCutoffManifestRule>(
            CappedCutoffManifestLimitRangeRule(80, 86400),
            CappedCutoffManifestAllowedDescriptionRule(mutableListOf<String>("weighted", "scheduled")),
            CappedCutoffManifestDriverIdLengthRule(1000),
            CappedCutoffManifestLoadKgRangeRule(5.4, 3832.9),
            CappedCutoffManifestRequiredCodeRule("capped cutoff manifest needs this field"),
            CappedCutoffManifestRequiredOwnerIdRule("capped cutoff manifest needs an entry"),
        ),
    )
}
