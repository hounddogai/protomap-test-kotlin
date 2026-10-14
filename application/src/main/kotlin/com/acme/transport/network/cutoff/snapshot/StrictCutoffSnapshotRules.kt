package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a strict cutoff snapshot must pass. Check returns its problems, or none. */
interface StrictCutoffSnapshotRule {
    fun check(item: StrictCutoffSnapshot): MutableList<String>
}

data class StrictCutoffSnapshotRequiredCodeRule(var message: String) : StrictCutoffSnapshotRule {
    override fun check(item: StrictCutoffSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffSnapshotCapacityRangeRule(var min: Int, var max: Int) : StrictCutoffSnapshotRule {
    override fun check(item: StrictCutoffSnapshot): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffSnapshotCodePrefixRule(var prefix: String) : StrictCutoffSnapshotRule {
    override fun check(item: StrictCutoffSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffSnapshotAllowedLocaleRule(var allowed: MutableList<String>) : StrictCutoffSnapshotRule {
    override fun check(item: StrictCutoffSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffSnapshotPalletsRangeRule(var min: Int, var max: Int) : StrictCutoffSnapshotRule {
    override fun check(item: StrictCutoffSnapshot): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class StrictCutoffSnapshotCategoryLengthRule(var maxLength: Int) : StrictCutoffSnapshotRule {
    override fun check(item: StrictCutoffSnapshot): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict cutoff snapshot. */
data class StrictCutoffSnapshotValidator(var rules: MutableList<StrictCutoffSnapshotRule>) {
    fun validate(item: StrictCutoffSnapshot): MutableList<String> {
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

    fun requireValid(item: StrictCutoffSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCutoffSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCutoffSnapshotValidator(): StrictCutoffSnapshotValidator {
    return StrictCutoffSnapshotValidator(
        mutableListOf<StrictCutoffSnapshotRule>(
            StrictCutoffSnapshotRequiredCodeRule("strict cutoff snapshot needs this field"),
            StrictCutoffSnapshotCapacityRangeRule(32, 3600),
            StrictCutoffSnapshotCodePrefixRule("REF"),
            StrictCutoffSnapshotAllowedLocaleRule(mutableListOf<String>("tiered", "batched")),
            StrictCutoffSnapshotPalletsRangeRule(120, 3600),
            StrictCutoffSnapshotCategoryLengthRule(16),
        ),
    )
}
