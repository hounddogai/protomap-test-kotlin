package com.acme.transport.crewing.swap.variance

/** A rule a swap draft must pass. Check returns its problems, or none. */
interface SwapDraftRule {
    fun check(item: SwapDraft): MutableList<String>
}

data class SwapDraftFloorValueRangeRule(var min: Double, var max: Double) : SwapDraftRule {
    override fun check(item: SwapDraft): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class SwapDraftWeightRangeRule(var min: Double, var max: Double) : SwapDraftRule {
    override fun check(item: SwapDraft): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapDraftRequiredExternalRefRule(var message: String) : SwapDraftRule {
    override fun check(item: SwapDraft): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapDraftNameLengthRule(var maxLength: Int) : SwapDraftRule {
    override fun check(item: SwapDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap draft. */
data class SwapDraftValidator(var rules: MutableList<SwapDraftRule>) {
    fun validate(item: SwapDraft): MutableList<String> {
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

    fun requireValid(item: SwapDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapDraftValidator(): SwapDraftValidator {
    return SwapDraftValidator(
        mutableListOf<SwapDraftRule>(
            SwapDraftFloorValueRangeRule(2.5, 4495.2),
            SwapDraftWeightRangeRule(2.2, 887.0),
            SwapDraftRequiredExternalRefRule("swap draft needs this field"),
            SwapDraftNameLengthRule(80),
        ),
    )
}
