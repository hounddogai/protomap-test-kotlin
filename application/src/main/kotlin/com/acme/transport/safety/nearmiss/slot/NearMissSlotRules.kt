package com.acme.transport.safety.nearmiss.slot

/** A rule a near miss slot must pass. Check returns its problems, or none. */
interface NearMissSlotRule {
    fun check(item: NearMissSlot): MutableList<String>
}

data class NearMissSlotCodePrefixRule(var prefix: String) : NearMissSlotRule {
    override fun check(item: NearMissSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissSlotDiscountRangeRule(var min: Double, var max: Double) : NearMissSlotRule {
    override fun check(item: NearMissSlot): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissSlotContactEmailLengthRule(var maxLength: Int) : NearMissSlotRule {
    override fun check(item: NearMissSlot): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissSlotRequiredSummaryRule(var message: String) : NearMissSlotRule {
    override fun check(item: NearMissSlot): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss slot. */
data class NearMissSlotValidator(var rules: MutableList<NearMissSlotRule>) {
    fun validate(item: NearMissSlot): MutableList<String> {
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

    fun requireValid(item: NearMissSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissSlotValidator(): NearMissSlotValidator {
    return NearMissSlotValidator(
        mutableListOf<NearMissSlotRule>(
            NearMissSlotCodePrefixRule("Q"),
            NearMissSlotDiscountRangeRule(4.3, 3823.5),
            NearMissSlotContactEmailLengthRule(120),
            NearMissSlotRequiredSummaryRule("near miss slot needs a value"),
        ),
    )
}
