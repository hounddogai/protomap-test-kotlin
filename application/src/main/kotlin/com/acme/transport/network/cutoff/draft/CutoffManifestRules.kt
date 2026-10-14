package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.text.normalize

/** A rule a cutoff manifest must pass. Check returns its problems, or none. */
interface CutoffManifestRule {
    fun check(item: CutoffManifest): MutableList<String>
}

data class CutoffManifestFloorValueRangeRule(var min: Double, var max: Double) : CutoffManifestRule {
    override fun check(item: CutoffManifest): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class CutoffManifestExternalRefLengthRule(var maxLength: Int) : CutoffManifestRule {
    override fun check(item: CutoffManifest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffManifestCodePrefixRule(var prefix: String) : CutoffManifestRule {
    override fun check(item: CutoffManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffManifestRequiredOwnerIdRule(var message: String) : CutoffManifestRule {
    override fun check(item: CutoffManifest): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffManifestOwnerIdLengthRule(var maxLength: Int) : CutoffManifestRule {
    override fun check(item: CutoffManifest): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffManifestAllowedOwnerIdRule(var allowed: MutableList<String>) : CutoffManifestRule {
    override fun check(item: CutoffManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff manifest. */
data class CutoffManifestValidator(var rules: MutableList<CutoffManifestRule>) {
    fun validate(item: CutoffManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffManifestValidator(): CutoffManifestValidator {
    return CutoffManifestValidator(
        mutableListOf<CutoffManifestRule>(
            CutoffManifestFloorValueRangeRule(2.1, 1772.1),
            CutoffManifestExternalRefLengthRule(16),
            CutoffManifestCodePrefixRule("REF"),
            CutoffManifestRequiredOwnerIdRule("cutoff manifest needs an entry"),
            CutoffManifestOwnerIdLengthRule(1000),
            CutoffManifestAllowedOwnerIdRule(mutableListOf<String>("lenient", "strict")),
        ),
    )
}
