package com.acme.transport.network.zone.threshold

/** A rule a regional zone segment must pass. Check returns its problems, or none. */
interface RegionalZoneSegmentRule {
    fun check(item: RegionalZoneSegment): MutableList<String>
}

data class RegionalZoneSegmentRequiredLabelRule(var message: String) : RegionalZoneSegmentRule {
    override fun check(item: RegionalZoneSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneSegmentRequiredUnitRule(var message: String) : RegionalZoneSegmentRule {
    override fun check(item: RegionalZoneSegment): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneSegmentRequiredCodeRule(var message: String) : RegionalZoneSegmentRule {
    override fun check(item: RegionalZoneSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional zone segment. */
data class RegionalZoneSegmentValidator(var rules: MutableList<RegionalZoneSegmentRule>) {
    fun validate(item: RegionalZoneSegment): MutableList<String> {
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

    fun requireValid(item: RegionalZoneSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalZoneSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalZoneSegmentValidator(): RegionalZoneSegmentValidator {
    return RegionalZoneSegmentValidator(
        mutableListOf<RegionalZoneSegmentRule>(
            RegionalZoneSegmentRequiredLabelRule("regional zone segment needs this field"),
            RegionalZoneSegmentRequiredUnitRule("regional zone segment needs this field"),
            RegionalZoneSegmentRequiredCodeRule("regional zone segment needs a value"),
        ),
    )
}
