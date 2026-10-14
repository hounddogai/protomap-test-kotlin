package com.acme.transport.safety.incident.incidentplan

/** A rule a fallback incident slot must pass. Check returns its problems, or none. */
interface FallbackIncidentSlotRule {
    fun check(item: FallbackIncidentSlot): MutableList<String>
}

data class FallbackIncidentSlotRequiredTitleRule(var message: String) : FallbackIncidentSlotRule {
    override fun check(item: FallbackIncidentSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentSlotEndsAtRangeRule(var min: Int, var max: Int) : FallbackIncidentSlotRule {
    override fun check(item: FallbackIncidentSlot): MutableList<String> {
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

data class FallbackIncidentSlotDepotLengthRule(var maxLength: Int) : FallbackIncidentSlotRule {
    override fun check(item: FallbackIncidentSlot): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentSlotRequiredCodeRule(var message: String) : FallbackIncidentSlotRule {
    override fun check(item: FallbackIncidentSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentSlotTitleLengthRule(var maxLength: Int) : FallbackIncidentSlotRule {
    override fun check(item: FallbackIncidentSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentSlotCodeLengthRule(var maxLength: Int) : FallbackIncidentSlotRule {
    override fun check(item: FallbackIncidentSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback incident slot. */
data class FallbackIncidentSlotValidator(var rules: MutableList<FallbackIncidentSlotRule>) {
    fun validate(item: FallbackIncidentSlot): MutableList<String> {
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

    fun requireValid(item: FallbackIncidentSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackIncidentSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackIncidentSlotValidator(): FallbackIncidentSlotValidator {
    return FallbackIncidentSlotValidator(
        mutableListOf<FallbackIncidentSlotRule>(
            FallbackIncidentSlotRequiredTitleRule("fallback incident slot needs a value"),
            FallbackIncidentSlotEndsAtRangeRule(255, 255),
            FallbackIncidentSlotDepotLengthRule(3600),
            FallbackIncidentSlotRequiredCodeRule("fallback incident slot needs an entry"),
            FallbackIncidentSlotTitleLengthRule(80),
            FallbackIncidentSlotCodeLengthRule(64),
        ),
    )
}
