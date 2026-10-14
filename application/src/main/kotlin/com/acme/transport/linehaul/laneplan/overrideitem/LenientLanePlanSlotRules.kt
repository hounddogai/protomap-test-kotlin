package com.acme.transport.linehaul.laneplan.overrideitem

/** A rule a lenient lane plan slot must pass. Check returns its problems, or none. */
interface LenientLanePlanSlotRule {
    fun check(item: LenientLanePlanSlot): MutableList<String>
}

data class LenientLanePlanSlotRequiredCodeRule(var message: String) : LenientLanePlanSlotRule {
    override fun check(item: LenientLanePlanSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanSlotEndsAtRangeRule(var min: Int, var max: Int) : LenientLanePlanSlotRule {
    override fun check(item: LenientLanePlanSlot): MutableList<String> {
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

data class LenientLanePlanSlotRequiredTrailerIdRule(var message: String) : LenientLanePlanSlotRule {
    override fun check(item: LenientLanePlanSlot): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient lane plan slot. */
data class LenientLanePlanSlotValidator(var rules: MutableList<LenientLanePlanSlotRule>) {
    fun validate(item: LenientLanePlanSlot): MutableList<String> {
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

    fun requireValid(item: LenientLanePlanSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientLanePlanSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientLanePlanSlotValidator(): LenientLanePlanSlotValidator {
    return LenientLanePlanSlotValidator(
        mutableListOf<LenientLanePlanSlotRule>(
            LenientLanePlanSlotRequiredCodeRule("lenient lane plan slot needs this field"),
            LenientLanePlanSlotEndsAtRangeRule(64, 80),
            LenientLanePlanSlotRequiredTrailerIdRule("lenient lane plan slot needs this field"),
        ),
    )
}
