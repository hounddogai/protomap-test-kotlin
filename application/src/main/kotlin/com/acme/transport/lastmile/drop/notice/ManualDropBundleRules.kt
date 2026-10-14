package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.text.normalize

/** A rule a manual drop bundle must pass. Check returns its problems, or none. */
interface ManualDropBundleRule {
    fun check(item: ManualDropBundle): MutableList<String>
}

data class ManualDropBundleCodePrefixRule(var prefix: String) : ManualDropBundleRule {
    override fun check(item: ManualDropBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDropBundleQuantityRangeRule(var min: Int, var max: Int) : ManualDropBundleRule {
    override fun check(item: ManualDropBundle): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualDropBundleAllowedNameRule(var allowed: MutableList<String>) : ManualDropBundleRule {
    override fun check(item: ManualDropBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ManualDropBundleNameLengthRule(var maxLength: Int) : ManualDropBundleRule {
    override fun check(item: ManualDropBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDropBundlePinnedGuardRule(var limit: Int) : ManualDropBundleRule {
    override fun check(item: ManualDropBundle): MutableList<String> {
        if (item.pinned && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ManualDropBundleRequiredNameRule(var message: String) : ManualDropBundleRule {
    override fun check(item: ManualDropBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual drop bundle. */
data class ManualDropBundleValidator(var rules: MutableList<ManualDropBundleRule>) {
    fun validate(item: ManualDropBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDropBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDropBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDropBundleValidator(): ManualDropBundleValidator {
    return ManualDropBundleValidator(
        mutableListOf<ManualDropBundleRule>(
            ManualDropBundleCodePrefixRule("REF"),
            ManualDropBundleQuantityRangeRule(16, 64),
            ManualDropBundleAllowedNameRule(mutableListOf<String>("adaptive", "manual")),
            ManualDropBundleNameLengthRule(3600),
            ManualDropBundlePinnedGuardRule(86400),
            ManualDropBundleRequiredNameRule("manual drop bundle needs this field"),
        ),
    )
}
