package com.acme.transport.network.zone.zonequota

/** A rule a seasonal zone policy must pass. Check returns its problems, or none. */
interface SeasonalZonePolicyRule {
    fun check(item: SeasonalZonePolicy): MutableList<String>
}

data class SeasonalZonePolicyRequiredNameRule(var message: String) : SeasonalZonePolicyRule {
    override fun check(item: SeasonalZonePolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZonePolicyRouteCodeLengthRule(var maxLength: Int) : SeasonalZonePolicyRule {
    override fun check(item: SeasonalZonePolicy): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZonePolicySummaryLengthRule(var maxLength: Int) : SeasonalZonePolicyRule {
    override fun check(item: SeasonalZonePolicy): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZonePolicyRequiredCodeRule(var message: String) : SeasonalZonePolicyRule {
    override fun check(item: SeasonalZonePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZonePolicyCodePrefixRule(var prefix: String) : SeasonalZonePolicyRule {
    override fun check(item: SeasonalZonePolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZonePolicyRequiredSummaryRule(var message: String) : SeasonalZonePolicyRule {
    override fun check(item: SeasonalZonePolicy): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal zone policy. */
data class SeasonalZonePolicyValidator(var rules: MutableList<SeasonalZonePolicyRule>) {
    fun validate(item: SeasonalZonePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalZonePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalZonePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalZonePolicyValidator(): SeasonalZonePolicyValidator {
    return SeasonalZonePolicyValidator(
        mutableListOf<SeasonalZonePolicyRule>(
            SeasonalZonePolicyRequiredNameRule("seasonal zone policy needs this field"),
            SeasonalZonePolicyRouteCodeLengthRule(16),
            SeasonalZonePolicySummaryLengthRule(1000),
            SeasonalZonePolicyRequiredCodeRule("seasonal zone policy needs a value"),
            SeasonalZonePolicyCodePrefixRule("OPS"),
            SeasonalZonePolicyRequiredSummaryRule("seasonal zone policy needs this field"),
        ),
    )
}
