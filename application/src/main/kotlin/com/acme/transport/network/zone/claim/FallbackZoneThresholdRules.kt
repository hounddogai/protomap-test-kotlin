package com.acme.transport.network.zone.claim

/** A rule a fallback zone threshold must pass. Check returns its problems, or none. */
interface FallbackZoneThresholdRule {
    fun check(item: FallbackZoneThreshold): MutableList<String>
}

data class FallbackZoneThresholdCodeLengthRule(var maxLength: Int) : FallbackZoneThresholdRule {
    override fun check(item: FallbackZoneThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackZoneThresholdRatioRangeRule(var min: Double, var max: Double) : FallbackZoneThresholdRule {
    override fun check(item: FallbackZoneThreshold): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class FallbackZoneThresholdRequiredUnitRule(var message: String) : FallbackZoneThresholdRule {
    override fun check(item: FallbackZoneThreshold): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback zone threshold. */
data class FallbackZoneThresholdValidator(var rules: MutableList<FallbackZoneThresholdRule>) {
    fun validate(item: FallbackZoneThreshold): MutableList<String> {
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

    fun requireValid(item: FallbackZoneThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackZoneThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackZoneThresholdValidator(): FallbackZoneThresholdValidator {
    return FallbackZoneThresholdValidator(
        mutableListOf<FallbackZoneThresholdRule>(
            FallbackZoneThresholdCodeLengthRule(1000),
            FallbackZoneThresholdRatioRangeRule(2.8, 3417.5),
            FallbackZoneThresholdRequiredUnitRule("fallback zone threshold needs this field"),
        ),
    )
}
