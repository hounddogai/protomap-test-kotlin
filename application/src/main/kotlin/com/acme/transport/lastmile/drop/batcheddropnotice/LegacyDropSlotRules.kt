package com.acme.transport.lastmile.drop.batcheddropnotice

/** A rule a legacy drop slot must pass. Check returns its problems, or none. */
interface LegacyDropSlotRule {
    fun check(item: LegacyDropSlot): MutableList<String>
}

data class LegacyDropSlotArchivedGuardRule(var limit: Double) : LegacyDropSlotRule {
    override fun check(item: LegacyDropSlot): MutableList<String> {
        if (item.archived && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDropSlotRequiredCodeRule(var message: String) : LegacyDropSlotRule {
    override fun check(item: LegacyDropSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDropSlotCodePrefixRule(var prefix: String) : LegacyDropSlotRule {
    override fun check(item: LegacyDropSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy drop slot. */
data class LegacyDropSlotValidator(var rules: MutableList<LegacyDropSlotRule>) {
    fun validate(item: LegacyDropSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDropSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDropSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDropSlotValidator(): LegacyDropSlotValidator {
    return LegacyDropSlotValidator(
        mutableListOf<LegacyDropSlotRule>(
            LegacyDropSlotArchivedGuardRule(6.6),
            LegacyDropSlotRequiredCodeRule("legacy drop slot needs this field"),
            LegacyDropSlotCodePrefixRule("Q"),
        ),
    )
}
