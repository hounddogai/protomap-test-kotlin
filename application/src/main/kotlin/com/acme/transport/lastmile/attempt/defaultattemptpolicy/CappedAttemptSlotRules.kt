package com.acme.transport.lastmile.attempt.defaultattemptpolicy

/** A rule a capped attempt slot must pass. Check returns its problems, or none. */
interface CappedAttemptSlotRule {
    fun check(item: CappedAttemptSlot): MutableList<String>
}

data class CappedAttemptSlotVisibleGuardRule(var limit: Int) : CappedAttemptSlotRule {
    override fun check(item: CappedAttemptSlot): MutableList<String> {
        if (item.visible && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptSlotRequiredDescriptionRule(var message: String) : CappedAttemptSlotRule {
    override fun check(item: CappedAttemptSlot): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptSlotCodeLengthRule(var maxLength: Int) : CappedAttemptSlotRule {
    override fun check(item: CappedAttemptSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped attempt slot. */
data class CappedAttemptSlotValidator(var rules: MutableList<CappedAttemptSlotRule>) {
    fun validate(item: CappedAttemptSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedAttemptSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAttemptSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAttemptSlotValidator(): CappedAttemptSlotValidator {
    return CappedAttemptSlotValidator(
        mutableListOf<CappedAttemptSlotRule>(
            CappedAttemptSlotVisibleGuardRule(32),
            CappedAttemptSlotRequiredDescriptionRule("capped attempt slot needs an entry"),
            CappedAttemptSlotCodeLengthRule(80),
        ),
    )
}
