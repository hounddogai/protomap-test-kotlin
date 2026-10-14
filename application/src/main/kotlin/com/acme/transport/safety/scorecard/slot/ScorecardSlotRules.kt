package com.acme.transport.safety.scorecard.slot

/** A rule a scorecard slot must pass. Check returns its problems, or none. */
interface ScorecardSlotRule {
    fun check(item: ScorecardSlot): MutableList<String>
}

data class ScorecardSlotNameLengthRule(var maxLength: Int) : ScorecardSlotRule {
    override fun check(item: ScorecardSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSlotRequiredCodeRule(var message: String) : ScorecardSlotRule {
    override fun check(item: ScorecardSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSlotLabelLengthRule(var maxLength: Int) : ScorecardSlotRule {
    override fun check(item: ScorecardSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard slot. */
data class ScorecardSlotValidator(var rules: MutableList<ScorecardSlotRule>) {
    fun validate(item: ScorecardSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardSlotValidator(): ScorecardSlotValidator {
    return ScorecardSlotValidator(
        mutableListOf<ScorecardSlotRule>(
            ScorecardSlotNameLengthRule(3600),
            ScorecardSlotRequiredCodeRule("scorecard slot needs an entry"),
            ScorecardSlotLabelLengthRule(16),
        ),
    )
}
