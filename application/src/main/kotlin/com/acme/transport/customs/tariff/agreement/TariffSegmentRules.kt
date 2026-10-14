package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.normalize

/** A rule a tariff segment must pass. Check returns its problems, or none. */
interface TariffSegmentRule {
    fun check(item: TariffSegment): MutableList<String>
}

data class TariffSegmentSourceLengthRule(var maxLength: Int) : TariffSegmentRule {
    override fun check(item: TariffSegment): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffSegmentUnitLengthRule(var maxLength: Int) : TariffSegmentRule {
    override fun check(item: TariffSegment): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffSegmentCodePrefixRule(var prefix: String) : TariffSegmentRule {
    override fun check(item: TariffSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffSegmentAllowedSourceRule(var allowed: MutableList<String>) : TariffSegmentRule {
    override fun check(item: TariffSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TariffSegmentRequiredCodeRule(var message: String) : TariffSegmentRule {
    override fun check(item: TariffSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffSegmentDistanceKmRangeRule(var min: Double, var max: Double) : TariffSegmentRule {
    override fun check(item: TariffSegment): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tariff segment. */
data class TariffSegmentValidator(var rules: MutableList<TariffSegmentRule>) {
    fun validate(item: TariffSegment): MutableList<String> {
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

    fun requireValid(item: TariffSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffSegmentValidator(): TariffSegmentValidator {
    return TariffSegmentValidator(
        mutableListOf<TariffSegmentRule>(
            TariffSegmentSourceLengthRule(80),
            TariffSegmentUnitLengthRule(64),
            TariffSegmentCodePrefixRule("X"),
            TariffSegmentAllowedSourceRule(mutableListOf<String>("legacy", "manual")),
            TariffSegmentRequiredCodeRule("tariff segment needs this field"),
            TariffSegmentDistanceKmRangeRule(3.6, 4930.0),
        ),
    )
}
