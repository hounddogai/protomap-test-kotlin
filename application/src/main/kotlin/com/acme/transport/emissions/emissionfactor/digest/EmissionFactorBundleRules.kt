package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.text.normalize

/** A rule a emission factor bundle must pass. Check returns its problems, or none. */
interface EmissionFactorBundleRule {
    fun check(item: EmissionFactorBundle): MutableList<String>
}

data class EmissionFactorBundleRequiredNameRule(var message: String) : EmissionFactorBundleRule {
    override fun check(item: EmissionFactorBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBundleNameLengthRule(var maxLength: Int) : EmissionFactorBundleRule {
    override fun check(item: EmissionFactorBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBundlePinnedGuardRule(var limit: Double) : EmissionFactorBundleRule {
    override fun check(item: EmissionFactorBundle): MutableList<String> {
        if (item.pinned && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBundleCodePrefixRule(var prefix: String) : EmissionFactorBundleRule {
    override fun check(item: EmissionFactorBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBundleCodeLengthRule(var maxLength: Int) : EmissionFactorBundleRule {
    override fun check(item: EmissionFactorBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBundleAllowedCodeRule(var allowed: MutableList<String>) : EmissionFactorBundleRule {
    override fun check(item: EmissionFactorBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor bundle. */
data class EmissionFactorBundleValidator(var rules: MutableList<EmissionFactorBundleRule>) {
    fun validate(item: EmissionFactorBundle): MutableList<String> {
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

    fun requireValid(item: EmissionFactorBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorBundleValidator(): EmissionFactorBundleValidator {
    return EmissionFactorBundleValidator(
        mutableListOf<EmissionFactorBundleRule>(
            EmissionFactorBundleRequiredNameRule("emission factor bundle needs this field"),
            EmissionFactorBundleNameLengthRule(255),
            EmissionFactorBundlePinnedGuardRule(1.8),
            EmissionFactorBundleCodePrefixRule("ACM"),
            EmissionFactorBundleCodeLengthRule(255),
            EmissionFactorBundleAllowedCodeRule(mutableListOf<String>("default", "weighted", "batched")),
        ),
    )
}
