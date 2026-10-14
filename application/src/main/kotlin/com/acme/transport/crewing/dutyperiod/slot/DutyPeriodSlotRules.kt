package com.acme.transport.crewing.dutyperiod.slot

/** A rule a duty period slot must pass. Check returns its problems, or none. */
interface DutyPeriodSlotRule {
    fun check(item: DutyPeriodSlot): MutableList<String>
}

data class DutyPeriodSlotRequiredNameRule(var message: String) : DutyPeriodSlotRule {
    override fun check(item: DutyPeriodSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodSlotRequiredCountryRule(var message: String) : DutyPeriodSlotRule {
    override fun check(item: DutyPeriodSlot): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodSlotLoadKgRangeRule(var min: Double, var max: Double) : DutyPeriodSlotRule {
    override fun check(item: DutyPeriodSlot): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodSlotRegionLengthRule(var maxLength: Int) : DutyPeriodSlotRule {
    override fun check(item: DutyPeriodSlot): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodSlotRequiredRegionRule(var message: String) : DutyPeriodSlotRule {
    override fun check(item: DutyPeriodSlot): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period slot. */
data class DutyPeriodSlotValidator(var rules: MutableList<DutyPeriodSlotRule>) {
    fun validate(item: DutyPeriodSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodSlotValidator(): DutyPeriodSlotValidator {
    return DutyPeriodSlotValidator(
        mutableListOf<DutyPeriodSlotRule>(
            DutyPeriodSlotRequiredNameRule("duty period slot needs a value"),
            DutyPeriodSlotRequiredCountryRule("duty period slot needs this field"),
            DutyPeriodSlotLoadKgRangeRule(1.2, 4331.6),
            DutyPeriodSlotRegionLengthRule(64),
            DutyPeriodSlotRequiredRegionRule("duty period slot needs a value"),
        ),
    )
}
