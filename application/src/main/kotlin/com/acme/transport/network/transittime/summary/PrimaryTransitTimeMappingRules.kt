package com.acme.transport.network.transittime.summary

/** A rule a primary transit time mapping must pass. Check returns its problems, or none. */
interface PrimaryTransitTimeMappingRule {
    fun check(item: PrimaryTransitTimeMapping): MutableList<String>
}

data class PrimaryTransitTimeMappingTrailerIdLengthRule(var maxLength: Int) : PrimaryTransitTimeMappingRule {
    override fun check(item: PrimaryTransitTimeMapping): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeMappingEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryTransitTimeMappingRule {
    override fun check(item: PrimaryTransitTimeMapping): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class PrimaryTransitTimeMappingCapacityRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryTransitTimeMappingRule {
    override fun check(item: PrimaryTransitTimeMapping): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary transit time mapping. */
data class PrimaryTransitTimeMappingValidator(var rules: MutableList<PrimaryTransitTimeMappingRule>) {
    fun validate(item: PrimaryTransitTimeMapping): MutableList<String> {
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

    fun requireValid(item: PrimaryTransitTimeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTransitTimeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTransitTimeMappingValidator(): PrimaryTransitTimeMappingValidator {
    return PrimaryTransitTimeMappingValidator(
        mutableListOf<PrimaryTransitTimeMappingRule>(
            PrimaryTransitTimeMappingTrailerIdLengthRule(64),
            PrimaryTransitTimeMappingEndsAtRangeRule(120, 1000),
            PrimaryTransitTimeMappingCapacityRangeRule(255, 1000),
        ),
    )
}
