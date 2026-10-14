package com.acme.transport.network.cutoff.slot

/** A rule a cutoff slot must pass. Check returns its problems, or none. */
interface CutoffSlotRule {
    fun check(item: CutoffSlot): MutableList<String>
}

data class CutoffSlotPriorityRangeRule(var min: Int, var max: Int) : CutoffSlotRule {
    override fun check(item: CutoffSlot): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class CutoffSlotApprovedGuardRule(var limit: Int) : CutoffSlotRule {
    override fun check(item: CutoffSlot): MutableList<String> {
        if (item.approved && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CutoffSlotTitleLengthRule(var maxLength: Int) : CutoffSlotRule {
    override fun check(item: CutoffSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffSlotRequiredReviewerRule(var message: String) : CutoffSlotRule {
    override fun check(item: CutoffSlot): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff slot. */
data class CutoffSlotValidator(var rules: MutableList<CutoffSlotRule>) {
    fun validate(item: CutoffSlot): MutableList<String> {
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

    fun requireValid(item: CutoffSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffSlotValidator(): CutoffSlotValidator {
    return CutoffSlotValidator(
        mutableListOf<CutoffSlotRule>(
            CutoffSlotPriorityRangeRule(80, 1000),
            CutoffSlotApprovedGuardRule(3600),
            CutoffSlotTitleLengthRule(64),
            CutoffSlotRequiredReviewerRule("cutoff slot needs this field"),
        ),
    )
}
