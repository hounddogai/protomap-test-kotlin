package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.kit.text.normalize

/** A rule a regional rate card snapshot must pass. Check returns its problems, or none. */
interface RegionalRateCardSnapshotRule {
    fun check(item: RegionalRateCardSnapshot): MutableList<String>
}

data class RegionalRateCardSnapshotAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : RegionalRateCardSnapshotRule {
    override fun check(item: RegionalRateCardSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardSnapshotRequiredNameRule(var message: String) : RegionalRateCardSnapshotRule {
    override fun check(item: RegionalRateCardSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardSnapshotCeilingRangeRule(
    var min: Double,
    var max: Double,
) : RegionalRateCardSnapshotRule {
    override fun check(item: RegionalRateCardSnapshot): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardSnapshotTitleLengthRule(var maxLength: Int) : RegionalRateCardSnapshotRule {
    override fun check(item: RegionalRateCardSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardSnapshotCapacityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalRateCardSnapshotRule {
    override fun check(item: RegionalRateCardSnapshot): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class RegionalRateCardSnapshotRateRangeRule(
    var min: Double,
    var max: Double,
) : RegionalRateCardSnapshotRule {
    override fun check(item: RegionalRateCardSnapshot): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional rate card snapshot. */
data class RegionalRateCardSnapshotValidator(var rules: MutableList<RegionalRateCardSnapshotRule>) {
    fun validate(item: RegionalRateCardSnapshot): MutableList<String> {
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

    fun requireValid(item: RegionalRateCardSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRateCardSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRateCardSnapshotValidator(): RegionalRateCardSnapshotValidator {
    return RegionalRateCardSnapshotValidator(
        mutableListOf<RegionalRateCardSnapshotRule>(
            RegionalRateCardSnapshotAllowedDescriptionRule(mutableListOf<String>()),
            RegionalRateCardSnapshotRequiredNameRule("regional rate card snapshot needs this field"),
            RegionalRateCardSnapshotCeilingRangeRule(3.1, 700.8),
            RegionalRateCardSnapshotTitleLengthRule(1000),
            RegionalRateCardSnapshotCapacityRangeRule(64, 1000),
            RegionalRateCardSnapshotRateRangeRule(6.6, 355.9),
        ),
    )
}
