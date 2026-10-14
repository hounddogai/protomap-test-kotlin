package com.acme.transport.customs.broker.slot

/** A rule a capped broker slot must pass. Check returns its problems, or none. */
interface CappedBrokerSlotRule {
    fun check(item: CappedBrokerSlot): MutableList<String>
}

data class CappedBrokerSlotDepotLengthRule(var maxLength: Int) : CappedBrokerSlotRule {
    override fun check(item: CappedBrokerSlot): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerSlotNameLengthRule(var maxLength: Int) : CappedBrokerSlotRule {
    override fun check(item: CappedBrokerSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerSlotTrailerIdLengthRule(var maxLength: Int) : CappedBrokerSlotRule {
    override fun check(item: CappedBrokerSlot): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped broker slot. */
data class CappedBrokerSlotValidator(var rules: MutableList<CappedBrokerSlotRule>) {
    fun validate(item: CappedBrokerSlot): MutableList<String> {
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

    fun requireValid(item: CappedBrokerSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBrokerSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBrokerSlotValidator(): CappedBrokerSlotValidator {
    return CappedBrokerSlotValidator(
        mutableListOf<CappedBrokerSlotRule>(
            CappedBrokerSlotDepotLengthRule(1000),
            CappedBrokerSlotNameLengthRule(3600),
            CappedBrokerSlotTrailerIdLengthRule(32),
        ),
    )
}
