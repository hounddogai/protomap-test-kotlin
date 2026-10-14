package com.acme.transport.network.zone.segment

/** A rule a zone segment must pass. Check returns its problems, or none. */
interface ZoneSegmentRule {
    fun check(item: ZoneSegment): MutableList<String>
}

data class ZoneSegmentDistanceKmRangeRule(var min: Double, var max: Double) : ZoneSegmentRule {
    override fun check(item: ZoneSegment): MutableList<String> {
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

data class ZoneSegmentWeightRangeRule(var min: Double, var max: Double) : ZoneSegmentRule {
    override fun check(item: ZoneSegment): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class ZoneSegmentRequiredNameRule(var message: String) : ZoneSegmentRule {
    override fun check(item: ZoneSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneSegmentRequiredSegmentRule(var message: String) : ZoneSegmentRule {
    override fun check(item: ZoneSegment): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone segment. */
data class ZoneSegmentValidator(var rules: MutableList<ZoneSegmentRule>) {
    fun validate(item: ZoneSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneSegmentValidator(): ZoneSegmentValidator {
    return ZoneSegmentValidator(
        mutableListOf<ZoneSegmentRule>(
            ZoneSegmentDistanceKmRangeRule(2.2, 3434.4),
            ZoneSegmentWeightRangeRule(5.3, 703.8),
            ZoneSegmentRequiredNameRule("zone segment needs a value"),
            ZoneSegmentRequiredSegmentRule("zone segment needs an entry"),
        ),
    )
}
