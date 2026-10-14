package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.text.normalize

/** A rule a regional zone tier must pass. Check returns its problems, or none. */
interface RegionalZoneTierRule {
    fun check(item: RegionalZoneTier): MutableList<String>
}

data class RegionalZoneTierRequiredLabelRule(var message: String) : RegionalZoneTierRule {
    override fun check(item: RegionalZoneTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneTierAllowedSegmentRule(var allowed: MutableList<String>) : RegionalZoneTierRule {
    override fun check(item: RegionalZoneTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneTierAmountRangeRule(var min: Double, var max: Double) : RegionalZoneTierRule {
    override fun check(item: RegionalZoneTier): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class RegionalZoneTierRequiredCodeRule(var message: String) : RegionalZoneTierRule {
    override fun check(item: RegionalZoneTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneTierDurationMinutesRangeRule(var min: Int, var max: Int) : RegionalZoneTierRule {
    override fun check(item: RegionalZoneTier): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneTierCodePrefixRule(var prefix: String) : RegionalZoneTierRule {
    override fun check(item: RegionalZoneTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional zone tier. */
data class RegionalZoneTierValidator(var rules: MutableList<RegionalZoneTierRule>) {
    fun validate(item: RegionalZoneTier): MutableList<String> {
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

    fun requireValid(item: RegionalZoneTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalZoneTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalZoneTierValidator(): RegionalZoneTierValidator {
    return RegionalZoneTierValidator(
        mutableListOf<RegionalZoneTierRule>(
            RegionalZoneTierRequiredLabelRule("regional zone tier needs this field"),
            RegionalZoneTierAllowedSegmentRule(mutableListOf<String>("batched")),
            RegionalZoneTierAmountRangeRule(0.2, 4541.3),
            RegionalZoneTierRequiredCodeRule("regional zone tier needs a value"),
            RegionalZoneTierDurationMinutesRangeRule(120, 3600),
            RegionalZoneTierCodePrefixRule("REF"),
        ),
    )
}
