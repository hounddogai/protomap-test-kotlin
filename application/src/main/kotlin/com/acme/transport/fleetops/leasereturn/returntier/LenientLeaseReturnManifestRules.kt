package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.kit.text.normalize

/** A rule a lenient lease return manifest must pass. Check returns its problems, or none. */
interface LenientLeaseReturnManifestRule {
    fun check(item: LenientLeaseReturnManifest): MutableList<String>
}

data class LenientLeaseReturnManifestLimitRangeRule(
    var min: Int,
    var max: Int,
) : LenientLeaseReturnManifestRule {
    override fun check(item: LenientLeaseReturnManifest): MutableList<String> {
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

data class LenientLeaseReturnManifestLabelLengthRule(var maxLength: Int) : LenientLeaseReturnManifestRule {
    override fun check(item: LenientLeaseReturnManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLeaseReturnManifestTitleLengthRule(var maxLength: Int) : LenientLeaseReturnManifestRule {
    override fun check(item: LenientLeaseReturnManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLeaseReturnManifestCodeLengthRule(var maxLength: Int) : LenientLeaseReturnManifestRule {
    override fun check(item: LenientLeaseReturnManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLeaseReturnManifestRequiredDockRule(var message: String) : LenientLeaseReturnManifestRule {
    override fun check(item: LenientLeaseReturnManifest): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientLeaseReturnManifestAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientLeaseReturnManifestRule {
    override fun check(item: LenientLeaseReturnManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient lease return manifest. */
data class LenientLeaseReturnManifestValidator(var rules: MutableList<LenientLeaseReturnManifestRule>) {
    fun validate(item: LenientLeaseReturnManifest): MutableList<String> {
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

    fun requireValid(item: LenientLeaseReturnManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientLeaseReturnManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientLeaseReturnManifestValidator(): LenientLeaseReturnManifestValidator {
    return LenientLeaseReturnManifestValidator(
        mutableListOf<LenientLeaseReturnManifestRule>(
            LenientLeaseReturnManifestLimitRangeRule(16, 16),
            LenientLeaseReturnManifestLabelLengthRule(86400),
            LenientLeaseReturnManifestTitleLengthRule(120),
            LenientLeaseReturnManifestCodeLengthRule(86400),
            LenientLeaseReturnManifestRequiredDockRule("lenient lease return manifest needs a value"),
            LenientLeaseReturnManifestAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
