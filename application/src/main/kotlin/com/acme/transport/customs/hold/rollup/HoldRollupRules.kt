package com.acme.transport.customs.hold.rollup

/** A rule a hold rollup must pass. Check returns its problems, or none. */
interface HoldRollupRule {
    fun check(item: HoldRollup): MutableList<String>
}

data class HoldRollupAmountRangeRule(var min: Double, var max: Double) : HoldRollupRule {
    override fun check(item: HoldRollup): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class HoldRollupRequiredLabelRule(var message: String) : HoldRollupRule {
    override fun check(item: HoldRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldRollupPhoneLengthRule(var maxLength: Int) : HoldRollupRule {
    override fun check(item: HoldRollup): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldRollupCodePrefixRule(var prefix: String) : HoldRollupRule {
    override fun check(item: HoldRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HoldRollupCreatedAtRangeRule(var min: Int, var max: Int) : HoldRollupRule {
    override fun check(item: HoldRollup): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold rollup. */
data class HoldRollupValidator(var rules: MutableList<HoldRollupRule>) {
    fun validate(item: HoldRollup): MutableList<String> {
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

    fun requireValid(item: HoldRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldRollupValidator(): HoldRollupValidator {
    return HoldRollupValidator(
        mutableListOf<HoldRollupRule>(
            HoldRollupAmountRangeRule(8.9, 2096.0),
            HoldRollupRequiredLabelRule("hold rollup needs this field"),
            HoldRollupPhoneLengthRule(86400),
            HoldRollupCodePrefixRule("OPS"),
            HoldRollupCreatedAtRangeRule(255, 86400),
        ),
    )
}
