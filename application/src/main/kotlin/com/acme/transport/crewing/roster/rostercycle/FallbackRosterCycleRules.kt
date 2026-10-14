package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.kit.text.normalize

/** A rule a fallback roster cycle must pass. Check returns its problems, or none. */
interface FallbackRosterCycleRule {
    fun check(item: FallbackRosterCycle): MutableList<String>
}

data class FallbackRosterCycleRatioRangeRule(var min: Double, var max: Double) : FallbackRosterCycleRule {
    override fun check(item: FallbackRosterCycle): MutableList<String> {
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

data class FallbackRosterCycleAllowedTitleRule(var allowed: MutableList<String>) : FallbackRosterCycleRule {
    override fun check(item: FallbackRosterCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRosterCycleEtaMinutesRangeRule(var min: Int, var max: Int) : FallbackRosterCycleRule {
    override fun check(item: FallbackRosterCycle): MutableList<String> {
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

data class FallbackRosterCycleRequiredCarrierRule(var message: String) : FallbackRosterCycleRule {
    override fun check(item: FallbackRosterCycle): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRosterCyclePalletsRangeRule(var min: Int, var max: Int) : FallbackRosterCycleRule {
    override fun check(item: FallbackRosterCycle): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRosterCycleRequiredCodeRule(var message: String) : FallbackRosterCycleRule {
    override fun check(item: FallbackRosterCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback roster cycle. */
data class FallbackRosterCycleValidator(var rules: MutableList<FallbackRosterCycleRule>) {
    fun validate(item: FallbackRosterCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRosterCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRosterCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRosterCycleValidator(): FallbackRosterCycleValidator {
    return FallbackRosterCycleValidator(
        mutableListOf<FallbackRosterCycleRule>(
            FallbackRosterCycleRatioRangeRule(7.6, 2354.4),
            FallbackRosterCycleAllowedTitleRule(mutableListOf<String>("lenient", "rolling", "adaptive")),
            FallbackRosterCycleEtaMinutesRangeRule(32, 3600),
            FallbackRosterCycleRequiredCarrierRule("fallback roster cycle needs this field"),
            FallbackRosterCyclePalletsRangeRule(16, 255),
            FallbackRosterCycleRequiredCodeRule("fallback roster cycle needs an entry"),
        ),
    )
}
