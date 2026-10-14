package com.acme.transport.safety.cameraevent.eventslot

/** A rule a adaptive camera event rollup must pass. Check returns its problems, or none. */
interface AdaptiveCameraEventRollupRule {
    fun check(item: AdaptiveCameraEventRollup): MutableList<String>
}

data class AdaptiveCameraEventRollupPhoneLengthRule(var maxLength: Int) : AdaptiveCameraEventRollupRule {
    override fun check(item: AdaptiveCameraEventRollup): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventRollupCodePrefixRule(var prefix: String) : AdaptiveCameraEventRollupRule {
    override fun check(item: AdaptiveCameraEventRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventRollupDiscountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCameraEventRollupRule {
    override fun check(item: AdaptiveCameraEventRollup): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class AdaptiveCameraEventRollupBudgetRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCameraEventRollupRule {
    override fun check(item: AdaptiveCameraEventRollup): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive camera event rollup. */
data class AdaptiveCameraEventRollupValidator(var rules: MutableList<AdaptiveCameraEventRollupRule>) {
    fun validate(item: AdaptiveCameraEventRollup): MutableList<String> {
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

    fun requireValid(item: AdaptiveCameraEventRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCameraEventRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCameraEventRollupValidator(): AdaptiveCameraEventRollupValidator {
    return AdaptiveCameraEventRollupValidator(
        mutableListOf<AdaptiveCameraEventRollupRule>(
            AdaptiveCameraEventRollupPhoneLengthRule(86400),
            AdaptiveCameraEventRollupCodePrefixRule("Q"),
            AdaptiveCameraEventRollupDiscountRangeRule(7.9, 2516.5),
            AdaptiveCameraEventRollupBudgetRangeRule(0.6, 3120.2),
        ),
    )
}
