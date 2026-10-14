package com.acme.transport.fleetops.tire.slot

/** A rule a pending tire slot must pass. Check returns its problems, or none. */
interface PendingTireSlotRule {
    fun check(item: PendingTireSlot): MutableList<String>
}

data class PendingTireSlotRequiredCodeRule(var message: String) : PendingTireSlotRule {
    override fun check(item: PendingTireSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTireSlotNameLengthRule(var maxLength: Int) : PendingTireSlotRule {
    override fun check(item: PendingTireSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTireSlotRequiredNameRule(var message: String) : PendingTireSlotRule {
    override fun check(item: PendingTireSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tire slot. */
data class PendingTireSlotValidator(var rules: MutableList<PendingTireSlotRule>) {
    fun validate(item: PendingTireSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTireSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTireSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTireSlotValidator(): PendingTireSlotValidator {
    return PendingTireSlotValidator(
        mutableListOf<PendingTireSlotRule>(
            PendingTireSlotRequiredCodeRule("pending tire slot needs an entry"),
            PendingTireSlotNameLengthRule(32),
            PendingTireSlotRequiredNameRule("pending tire slot needs this field"),
        ),
    )
}
