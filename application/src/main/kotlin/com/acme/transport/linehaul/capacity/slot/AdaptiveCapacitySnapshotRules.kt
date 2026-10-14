package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.text.normalize

/** A rule a adaptive capacity snapshot must pass. Check returns its problems, or none. */
interface AdaptiveCapacitySnapshotRule {
    fun check(item: AdaptiveCapacitySnapshot): MutableList<String>
}

data class AdaptiveCapacitySnapshotRequiredLabelRule(var message: String) : AdaptiveCapacitySnapshotRule {
    override fun check(item: AdaptiveCapacitySnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacitySnapshotApprovedGuardRule(var limit: Double) : AdaptiveCapacitySnapshotRule {
    override fun check(item: AdaptiveCapacitySnapshot): MutableList<String> {
        if (item.approved && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacitySnapshotRequiredCurrencyRule(var message: String) : AdaptiveCapacitySnapshotRule {
    override fun check(item: AdaptiveCapacitySnapshot): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacitySnapshotLocaleLengthRule(var maxLength: Int) : AdaptiveCapacitySnapshotRule {
    override fun check(item: AdaptiveCapacitySnapshot): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacitySnapshotAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveCapacitySnapshotRule {
    override fun check(item: AdaptiveCapacitySnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive capacity snapshot. */
data class AdaptiveCapacitySnapshotValidator(var rules: MutableList<AdaptiveCapacitySnapshotRule>) {
    fun validate(item: AdaptiveCapacitySnapshot): MutableList<String> {
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

    fun requireValid(item: AdaptiveCapacitySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCapacitySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCapacitySnapshotValidator(): AdaptiveCapacitySnapshotValidator {
    return AdaptiveCapacitySnapshotValidator(
        mutableListOf<AdaptiveCapacitySnapshotRule>(
            AdaptiveCapacitySnapshotRequiredLabelRule("adaptive capacity snapshot needs an entry"),
            AdaptiveCapacitySnapshotApprovedGuardRule(0.8),
            AdaptiveCapacitySnapshotRequiredCurrencyRule("adaptive capacity snapshot needs this field"),
            AdaptiveCapacitySnapshotLocaleLengthRule(3600),
            AdaptiveCapacitySnapshotAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
