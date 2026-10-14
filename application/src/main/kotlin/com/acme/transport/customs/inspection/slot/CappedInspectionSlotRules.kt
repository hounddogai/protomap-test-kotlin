package com.acme.transport.customs.inspection.slot

/** A rule a capped inspection slot must pass. Check returns its problems, or none. */
interface CappedInspectionSlotRule {
    fun check(item: CappedInspectionSlot): MutableList<String>
}

data class CappedInspectionSlotRequiredTitleRule(var message: String) : CappedInspectionSlotRule {
    override fun check(item: CappedInspectionSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionSlotCountryLengthRule(var maxLength: Int) : CappedInspectionSlotRule {
    override fun check(item: CappedInspectionSlot): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionSlotScoreRangeRule(var min: Double, var max: Double) : CappedInspectionSlotRule {
    override fun check(item: CappedInspectionSlot): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped inspection slot. */
data class CappedInspectionSlotValidator(var rules: MutableList<CappedInspectionSlotRule>) {
    fun validate(item: CappedInspectionSlot): MutableList<String> {
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

    fun requireValid(item: CappedInspectionSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedInspectionSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedInspectionSlotValidator(): CappedInspectionSlotValidator {
    return CappedInspectionSlotValidator(
        mutableListOf<CappedInspectionSlotRule>(
            CappedInspectionSlotRequiredTitleRule("capped inspection slot needs an entry"),
            CappedInspectionSlotCountryLengthRule(16),
            CappedInspectionSlotScoreRangeRule(3.9, 2494.0),
        ),
    )
}
