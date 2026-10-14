package com.acme.transport.safety.incident.slot

/** A rule a capped incident slot must pass. Check returns its problems, or none. */
interface CappedIncidentSlotRule {
    fun check(item: CappedIncidentSlot): MutableList<String>
}

data class CappedIncidentSlotRequiredCodeRule(var message: String) : CappedIncidentSlotRule {
    override fun check(item: CappedIncidentSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentSlotAttemptsRangeRule(var min: Int, var max: Int) : CappedIncidentSlotRule {
    override fun check(item: CappedIncidentSlot): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class CappedIncidentSlotCapacityRangeRule(var min: Int, var max: Int) : CappedIncidentSlotRule {
    override fun check(item: CappedIncidentSlot): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped incident slot. */
data class CappedIncidentSlotValidator(var rules: MutableList<CappedIncidentSlotRule>) {
    fun validate(item: CappedIncidentSlot): MutableList<String> {
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

    fun requireValid(item: CappedIncidentSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedIncidentSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedIncidentSlotValidator(): CappedIncidentSlotValidator {
    return CappedIncidentSlotValidator(
        mutableListOf<CappedIncidentSlotRule>(
            CappedIncidentSlotRequiredCodeRule("capped incident slot needs this field"),
            CappedIncidentSlotAttemptsRangeRule(16, 3600),
            CappedIncidentSlotCapacityRangeRule(64, 3600),
        ),
    )
}
