package com.acme.transport.freightpay.accessorial.accessorialbudget

/** A rule a adaptive accessorial slot must pass. Check returns its problems, or none. */
interface AdaptiveAccessorialSlotRule {
    fun check(item: AdaptiveAccessorialSlot): MutableList<String>
}

data class AdaptiveAccessorialSlotThresholdRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveAccessorialSlotRule {
    override fun check(item: AdaptiveAccessorialSlot): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class AdaptiveAccessorialSlotCodePrefixRule(var prefix: String) : AdaptiveAccessorialSlotRule {
    override fun check(item: AdaptiveAccessorialSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialSlotDockLengthRule(var maxLength: Int) : AdaptiveAccessorialSlotRule {
    override fun check(item: AdaptiveAccessorialSlot): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialSlotCodeLengthRule(var maxLength: Int) : AdaptiveAccessorialSlotRule {
    override fun check(item: AdaptiveAccessorialSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive accessorial slot. */
data class AdaptiveAccessorialSlotValidator(var rules: MutableList<AdaptiveAccessorialSlotRule>) {
    fun validate(item: AdaptiveAccessorialSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAccessorialSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAccessorialSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAccessorialSlotValidator(): AdaptiveAccessorialSlotValidator {
    return AdaptiveAccessorialSlotValidator(
        mutableListOf<AdaptiveAccessorialSlotRule>(
            AdaptiveAccessorialSlotThresholdRangeRule(1.8, 1088.5),
            AdaptiveAccessorialSlotCodePrefixRule("REF"),
            AdaptiveAccessorialSlotDockLengthRule(64),
            AdaptiveAccessorialSlotCodeLengthRule(86400),
        ),
    )
}
