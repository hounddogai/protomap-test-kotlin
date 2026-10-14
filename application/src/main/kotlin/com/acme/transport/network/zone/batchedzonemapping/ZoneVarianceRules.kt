package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.text.normalize

/** A rule a zone variance must pass. Check returns its problems, or none. */
interface ZoneVarianceRule {
    fun check(item: ZoneVariance): MutableList<String>
}

data class ZoneVarianceAllowedTimezoneRule(var allowed: MutableList<String>) : ZoneVarianceRule {
    override fun check(item: ZoneVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ZoneVarianceRequiredRegionRule(var message: String) : ZoneVarianceRule {
    override fun check(item: ZoneVariance): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneVarianceMarginRangeRule(var min: Double, var max: Double) : ZoneVarianceRule {
    override fun check(item: ZoneVariance): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class ZoneVarianceRegionLengthRule(var maxLength: Int) : ZoneVarianceRule {
    override fun check(item: ZoneVariance): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneVarianceRequiredTimezoneRule(var message: String) : ZoneVarianceRule {
    override fun check(item: ZoneVariance): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneVarianceTimezoneLengthRule(var maxLength: Int) : ZoneVarianceRule {
    override fun check(item: ZoneVariance): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone variance. */
data class ZoneVarianceValidator(var rules: MutableList<ZoneVarianceRule>) {
    fun validate(item: ZoneVariance): MutableList<String> {
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

    fun requireValid(item: ZoneVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneVarianceValidator(): ZoneVarianceValidator {
    return ZoneVarianceValidator(
        mutableListOf<ZoneVarianceRule>(
            ZoneVarianceAllowedTimezoneRule(mutableListOf<String>("pending", "legacy")),
            ZoneVarianceRequiredRegionRule("zone variance needs a value"),
            ZoneVarianceMarginRangeRule(1.2, 2126.3),
            ZoneVarianceRegionLengthRule(255),
            ZoneVarianceRequiredTimezoneRule("zone variance needs an entry"),
            ZoneVarianceTimezoneLengthRule(64),
        ),
    )
}
