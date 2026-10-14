package com.acme.transport.network.zone.bundle

/** A rule a zone bundle must pass. Check returns its problems, or none. */
interface ZoneBundleRule {
    fun check(item: ZoneBundle): MutableList<String>
}

data class ZoneBundleCodePrefixRule(var prefix: String) : ZoneBundleRule {
    override fun check(item: ZoneBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneBundleLaneLengthRule(var maxLength: Int) : ZoneBundleRule {
    override fun check(item: ZoneBundle): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneBundleRequiredTrailerIdRule(var message: String) : ZoneBundleRule {
    override fun check(item: ZoneBundle): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneBundleAmountRangeRule(var min: Double, var max: Double) : ZoneBundleRule {
    override fun check(item: ZoneBundle): MutableList<String> {
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

data class ZoneBundleRequiredLaneRule(var message: String) : ZoneBundleRule {
    override fun check(item: ZoneBundle): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneBundleTrailerIdLengthRule(var maxLength: Int) : ZoneBundleRule {
    override fun check(item: ZoneBundle): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone bundle. */
data class ZoneBundleValidator(var rules: MutableList<ZoneBundleRule>) {
    fun validate(item: ZoneBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneBundleValidator(): ZoneBundleValidator {
    return ZoneBundleValidator(
        mutableListOf<ZoneBundleRule>(
            ZoneBundleCodePrefixRule("X"),
            ZoneBundleLaneLengthRule(120),
            ZoneBundleRequiredTrailerIdRule("zone bundle needs a value"),
            ZoneBundleAmountRangeRule(0.7, 218.2),
            ZoneBundleRequiredLaneRule("zone bundle needs a value"),
            ZoneBundleTrailerIdLengthRule(64),
        ),
    )
}
