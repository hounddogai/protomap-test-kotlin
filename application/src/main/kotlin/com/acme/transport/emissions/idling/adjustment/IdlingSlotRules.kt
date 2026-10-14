package com.acme.transport.emissions.idling.adjustment

/** A rule a idling slot must pass. Check returns its problems, or none. */
interface IdlingSlotRule {
    fun check(item: IdlingSlot): MutableList<String>
}

data class IdlingSlotTitleLengthRule(var maxLength: Int) : IdlingSlotRule {
    override fun check(item: IdlingSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingSlotRequiredContactEmailRule(var message: String) : IdlingSlotRule {
    override fun check(item: IdlingSlot): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingSlotContactEmailLengthRule(var maxLength: Int) : IdlingSlotRule {
    override fun check(item: IdlingSlot): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingSlotThresholdRangeRule(var min: Double, var max: Double) : IdlingSlotRule {
    override fun check(item: IdlingSlot): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingSlotRequiredCodeRule(var message: String) : IdlingSlotRule {
    override fun check(item: IdlingSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingSlotCodePrefixRule(var prefix: String) : IdlingSlotRule {
    override fun check(item: IdlingSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling slot. */
data class IdlingSlotValidator(var rules: MutableList<IdlingSlotRule>) {
    fun validate(item: IdlingSlot): MutableList<String> {
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

    fun requireValid(item: IdlingSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingSlotValidator(): IdlingSlotValidator {
    return IdlingSlotValidator(
        mutableListOf<IdlingSlotRule>(
            IdlingSlotTitleLengthRule(80),
            IdlingSlotRequiredContactEmailRule("idling slot needs this field"),
            IdlingSlotContactEmailLengthRule(1000),
            IdlingSlotThresholdRangeRule(8.9, 975.4),
            IdlingSlotRequiredCodeRule("idling slot needs this field"),
            IdlingSlotCodePrefixRule("REF"),
        ),
    )
}
