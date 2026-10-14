package com.acme.transport.emissions.routeoffset.baseline

/** A rule a route offset snapshot must pass. Check returns its problems, or none. */
interface RouteOffsetSnapshotRule {
    fun check(item: RouteOffsetSnapshot): MutableList<String>
}

data class RouteOffsetSnapshotCodePrefixRule(var prefix: String) : RouteOffsetSnapshotRule {
    override fun check(item: RouteOffsetSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSnapshotRequiredTrailerIdRule(var message: String) : RouteOffsetSnapshotRule {
    override fun check(item: RouteOffsetSnapshot): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSnapshotEtaMinutesRangeRule(var min: Int, var max: Int) : RouteOffsetSnapshotRule {
    override fun check(item: RouteOffsetSnapshot): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetSnapshotDurationMinutesRangeRule(var min: Int, var max: Int) : RouteOffsetSnapshotRule {
    override fun check(item: RouteOffsetSnapshot): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetSnapshotTrailerIdLengthRule(var maxLength: Int) : RouteOffsetSnapshotRule {
    override fun check(item: RouteOffsetSnapshot): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset snapshot. */
data class RouteOffsetSnapshotValidator(var rules: MutableList<RouteOffsetSnapshotRule>) {
    fun validate(item: RouteOffsetSnapshot): MutableList<String> {
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

    fun requireValid(item: RouteOffsetSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetSnapshotValidator(): RouteOffsetSnapshotValidator {
    return RouteOffsetSnapshotValidator(
        mutableListOf<RouteOffsetSnapshotRule>(
            RouteOffsetSnapshotCodePrefixRule("REF"),
            RouteOffsetSnapshotRequiredTrailerIdRule("route offset snapshot needs a value"),
            RouteOffsetSnapshotEtaMinutesRangeRule(1000, 3600),
            RouteOffsetSnapshotDurationMinutesRangeRule(80, 86400),
            RouteOffsetSnapshotTrailerIdLengthRule(32),
        ),
    )
}
