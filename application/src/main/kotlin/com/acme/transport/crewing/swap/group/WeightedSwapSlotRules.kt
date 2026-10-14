package com.acme.transport.crewing.swap.group

/** A rule a weighted swap slot must pass. Check returns its problems, or none. */
interface WeightedSwapSlotRule {
    fun check(item: WeightedSwapSlot): MutableList<String>
}

data class WeightedSwapSlotRequiredChecksumRule(var message: String) : WeightedSwapSlotRule {
    override fun check(item: WeightedSwapSlot): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapSlotChecksumLengthRule(var maxLength: Int) : WeightedSwapSlotRule {
    override fun check(item: WeightedSwapSlot): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapSlotVersionRangeRule(var min: Int, var max: Int) : WeightedSwapSlotRule {
    override fun check(item: WeightedSwapSlot): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapSlotCodePrefixRule(var prefix: String) : WeightedSwapSlotRule {
    override fun check(item: WeightedSwapSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapSlotRequiredCodeRule(var message: String) : WeightedSwapSlotRule {
    override fun check(item: WeightedSwapSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapSlotRequiredNameRule(var message: String) : WeightedSwapSlotRule {
    override fun check(item: WeightedSwapSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted swap slot. */
data class WeightedSwapSlotValidator(var rules: MutableList<WeightedSwapSlotRule>) {
    fun validate(item: WeightedSwapSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedSwapSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedSwapSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedSwapSlotValidator(): WeightedSwapSlotValidator {
    return WeightedSwapSlotValidator(
        mutableListOf<WeightedSwapSlotRule>(
            WeightedSwapSlotRequiredChecksumRule("weighted swap slot needs a value"),
            WeightedSwapSlotChecksumLengthRule(3600),
            WeightedSwapSlotVersionRangeRule(120, 3600),
            WeightedSwapSlotCodePrefixRule("X"),
            WeightedSwapSlotRequiredCodeRule("weighted swap slot needs this field"),
            WeightedSwapSlotRequiredNameRule("weighted swap slot needs this field"),
        ),
    )
}
