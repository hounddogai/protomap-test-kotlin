package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.normalize

/** A rule a legacy backhaul allocation must pass. Check returns its problems, or none. */
interface LegacyBackhaulAllocationRule {
    fun check(item: LegacyBackhaulAllocation): MutableList<String>
}

data class LegacyBackhaulAllocationRequiredNameRule(var message: String) : LegacyBackhaulAllocationRule {
    override fun check(item: LegacyBackhaulAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulAllocationCodePrefixRule(var prefix: String) : LegacyBackhaulAllocationRule {
    override fun check(item: LegacyBackhaulAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulAllocationCodeLengthRule(var maxLength: Int) : LegacyBackhaulAllocationRule {
    override fun check(item: LegacyBackhaulAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulAllocationLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : LegacyBackhaulAllocationRule {
    override fun check(item: LegacyBackhaulAllocation): MutableList<String> {
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

data class LegacyBackhaulAllocationNameLengthRule(var maxLength: Int) : LegacyBackhaulAllocationRule {
    override fun check(item: LegacyBackhaulAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulAllocationAllowedNameRule(
    var allowed: MutableList<String>,
) : LegacyBackhaulAllocationRule {
    override fun check(item: LegacyBackhaulAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy backhaul allocation. */
data class LegacyBackhaulAllocationValidator(var rules: MutableList<LegacyBackhaulAllocationRule>) {
    fun validate(item: LegacyBackhaulAllocation): MutableList<String> {
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

    fun requireValid(item: LegacyBackhaulAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBackhaulAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBackhaulAllocationValidator(): LegacyBackhaulAllocationValidator {
    return LegacyBackhaulAllocationValidator(
        mutableListOf<LegacyBackhaulAllocationRule>(
            LegacyBackhaulAllocationRequiredNameRule("legacy backhaul allocation needs an entry"),
            LegacyBackhaulAllocationCodePrefixRule("OPS"),
            LegacyBackhaulAllocationCodeLengthRule(1000),
            LegacyBackhaulAllocationLoadKgRangeRule(5.9, 4180.7),
            LegacyBackhaulAllocationNameLengthRule(255),
            LegacyBackhaulAllocationAllowedNameRule(mutableListOf<String>("batched")),
        ),
    )
}
