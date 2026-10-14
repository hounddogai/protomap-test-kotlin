package com.acme.transport.linehaul.backhaul.preference

/** A rule a tiered backhaul preference must pass. Check returns its problems, or none. */
interface TieredBackhaulPreferenceRule {
    fun check(item: TieredBackhaulPreference): MutableList<String>
}

data class TieredBackhaulPreferenceSequenceRangeRule(
    var min: Int,
    var max: Int,
) : TieredBackhaulPreferenceRule {
    override fun check(item: TieredBackhaulPreference): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredBackhaulPreferenceLimitRangeRule(var min: Int, var max: Int) : TieredBackhaulPreferenceRule {
    override fun check(item: TieredBackhaulPreference): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredBackhaulPreferenceRequiredRouteCodeRule(var message: String) : TieredBackhaulPreferenceRule {
    override fun check(item: TieredBackhaulPreference): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredBackhaulPreferenceRequiredExternalRefRule(var message: String) : TieredBackhaulPreferenceRule {
    override fun check(item: TieredBackhaulPreference): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredBackhaulPreferencePinnedGuardRule(var limit: Double) : TieredBackhaulPreferenceRule {
    override fun check(item: TieredBackhaulPreference): MutableList<String> {
        if (item.pinned && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class TieredBackhaulPreferenceRouteCodeLengthRule(var maxLength: Int) : TieredBackhaulPreferenceRule {
    override fun check(item: TieredBackhaulPreference): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered backhaul preference. */
data class TieredBackhaulPreferenceValidator(var rules: MutableList<TieredBackhaulPreferenceRule>) {
    fun validate(item: TieredBackhaulPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredBackhaulPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredBackhaulPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredBackhaulPreferenceValidator(): TieredBackhaulPreferenceValidator {
    return TieredBackhaulPreferenceValidator(
        mutableListOf<TieredBackhaulPreferenceRule>(
            TieredBackhaulPreferenceSequenceRangeRule(80, 3600),
            TieredBackhaulPreferenceLimitRangeRule(80, 1000),
            TieredBackhaulPreferenceRequiredRouteCodeRule("tiered backhaul preference needs this field"),
            TieredBackhaulPreferenceRequiredExternalRefRule("tiered backhaul preference needs this field"),
            TieredBackhaulPreferencePinnedGuardRule(0.3),
            TieredBackhaulPreferenceRouteCodeLengthRule(3600),
        ),
    )
}
