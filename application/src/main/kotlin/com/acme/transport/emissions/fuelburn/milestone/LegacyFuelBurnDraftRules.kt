package com.acme.transport.emissions.fuelburn.milestone

/** A rule a legacy fuel burn draft must pass. Check returns its problems, or none. */
interface LegacyFuelBurnDraftRule {
    fun check(item: LegacyFuelBurnDraft): MutableList<String>
}

data class LegacyFuelBurnDraftWeightRangeRule(var min: Double, var max: Double) : LegacyFuelBurnDraftRule {
    override fun check(item: LegacyFuelBurnDraft): MutableList<String> {
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

data class LegacyFuelBurnDraftPinnedGuardRule(var limit: Double) : LegacyFuelBurnDraftRule {
    override fun check(item: LegacyFuelBurnDraft): MutableList<String> {
        if (item.pinned && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelBurnDraftRateRangeRule(var min: Double, var max: Double) : LegacyFuelBurnDraftRule {
    override fun check(item: LegacyFuelBurnDraft): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class LegacyFuelBurnDraftRequiredCodeRule(var message: String) : LegacyFuelBurnDraftRule {
    override fun check(item: LegacyFuelBurnDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy fuel burn draft. */
data class LegacyFuelBurnDraftValidator(var rules: MutableList<LegacyFuelBurnDraftRule>) {
    fun validate(item: LegacyFuelBurnDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyFuelBurnDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyFuelBurnDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyFuelBurnDraftValidator(): LegacyFuelBurnDraftValidator {
    return LegacyFuelBurnDraftValidator(
        mutableListOf<LegacyFuelBurnDraftRule>(
            LegacyFuelBurnDraftWeightRangeRule(3.1, 1748.4),
            LegacyFuelBurnDraftPinnedGuardRule(4.0),
            LegacyFuelBurnDraftRateRangeRule(9.0, 1324.8),
            LegacyFuelBurnDraftRequiredCodeRule("legacy fuel burn draft needs this field"),
        ),
    )
}
