package com.acme.transport.fleetops.defect.policy

/** A rule a strict defect slot must pass. Check returns its problems, or none. */
interface StrictDefectSlotRule {
    fun check(item: StrictDefectSlot): MutableList<String>
}

data class StrictDefectSlotRequiredLocaleRule(var message: String) : StrictDefectSlotRule {
    override fun check(item: StrictDefectSlot): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDefectSlotRequiredNameRule(var message: String) : StrictDefectSlotRule {
    override fun check(item: StrictDefectSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDefectSlotRequiredSummaryRule(var message: String) : StrictDefectSlotRule {
    override fun check(item: StrictDefectSlot): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict defect slot. */
data class StrictDefectSlotValidator(var rules: MutableList<StrictDefectSlotRule>) {
    fun validate(item: StrictDefectSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictDefectSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDefectSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDefectSlotValidator(): StrictDefectSlotValidator {
    return StrictDefectSlotValidator(
        mutableListOf<StrictDefectSlotRule>(
            StrictDefectSlotRequiredLocaleRule("strict defect slot needs this field"),
            StrictDefectSlotRequiredNameRule("strict defect slot needs an entry"),
            StrictDefectSlotRequiredSummaryRule("strict defect slot needs an entry"),
        ),
    )
}
