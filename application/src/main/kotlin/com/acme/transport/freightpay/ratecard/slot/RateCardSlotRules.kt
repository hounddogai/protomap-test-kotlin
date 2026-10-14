package com.acme.transport.freightpay.ratecard.slot

/** A rule a rate card slot must pass. Check returns its problems, or none. */
interface RateCardSlotRule {
    fun check(item: RateCardSlot): MutableList<String>
}

data class RateCardSlotCodeLengthRule(var maxLength: Int) : RateCardSlotRule {
    override fun check(item: RateCardSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSlotCodePrefixRule(var prefix: String) : RateCardSlotRule {
    override fun check(item: RateCardSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSlotRequiredTitleRule(var message: String) : RateCardSlotRule {
    override fun check(item: RateCardSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card slot. */
data class RateCardSlotValidator(var rules: MutableList<RateCardSlotRule>) {
    fun validate(item: RateCardSlot): MutableList<String> {
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

    fun requireValid(item: RateCardSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardSlotValidator(): RateCardSlotValidator {
    return RateCardSlotValidator(
        mutableListOf<RateCardSlotRule>(
            RateCardSlotCodeLengthRule(120),
            RateCardSlotCodePrefixRule("Q"),
            RateCardSlotRequiredTitleRule("rate card slot needs a value"),
        ),
    )
}
