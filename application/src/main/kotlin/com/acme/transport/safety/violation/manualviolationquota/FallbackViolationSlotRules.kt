package com.acme.transport.safety.violation.manualviolationquota

/** A rule a fallback violation slot must pass. Check returns its problems, or none. */
interface FallbackViolationSlotRule {
    fun check(item: FallbackViolationSlot): MutableList<String>
}

data class FallbackViolationSlotTitleLengthRule(var maxLength: Int) : FallbackViolationSlotRule {
    override fun check(item: FallbackViolationSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationSlotCodePrefixRule(var prefix: String) : FallbackViolationSlotRule {
    override fun check(item: FallbackViolationSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationSlotRequiredCodeRule(var message: String) : FallbackViolationSlotRule {
    override fun check(item: FallbackViolationSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationSlotRequiredTitleRule(var message: String) : FallbackViolationSlotRule {
    override fun check(item: FallbackViolationSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationSlotLocaleLengthRule(var maxLength: Int) : FallbackViolationSlotRule {
    override fun check(item: FallbackViolationSlot): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback violation slot. */
data class FallbackViolationSlotValidator(var rules: MutableList<FallbackViolationSlotRule>) {
    fun validate(item: FallbackViolationSlot): MutableList<String> {
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

    fun requireValid(item: FallbackViolationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackViolationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackViolationSlotValidator(): FallbackViolationSlotValidator {
    return FallbackViolationSlotValidator(
        mutableListOf<FallbackViolationSlotRule>(
            FallbackViolationSlotTitleLengthRule(120),
            FallbackViolationSlotCodePrefixRule("Q"),
            FallbackViolationSlotRequiredCodeRule("fallback violation slot needs an entry"),
            FallbackViolationSlotRequiredTitleRule("fallback violation slot needs this field"),
            FallbackViolationSlotLocaleLengthRule(120),
        ),
    )
}
