package com.acme.transport.crewing.swap.estimate

/** A rule a swap assignment must pass. Check returns its problems, or none. */
interface SwapAssignmentRule {
    fun check(item: SwapAssignment): MutableList<String>
}

data class SwapAssignmentCodeLengthRule(var maxLength: Int) : SwapAssignmentRule {
    override fun check(item: SwapAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapAssignmentQuantityRangeRule(var min: Int, var max: Int) : SwapAssignmentRule {
    override fun check(item: SwapAssignment): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapAssignmentCodePrefixRule(var prefix: String) : SwapAssignmentRule {
    override fun check(item: SwapAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapAssignmentLabelLengthRule(var maxLength: Int) : SwapAssignmentRule {
    override fun check(item: SwapAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapAssignmentRequiredExternalRefRule(var message: String) : SwapAssignmentRule {
    override fun check(item: SwapAssignment): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap assignment. */
data class SwapAssignmentValidator(var rules: MutableList<SwapAssignmentRule>) {
    fun validate(item: SwapAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapAssignmentValidator(): SwapAssignmentValidator {
    return SwapAssignmentValidator(
        mutableListOf<SwapAssignmentRule>(
            SwapAssignmentCodeLengthRule(3600),
            SwapAssignmentQuantityRangeRule(3600, 86400),
            SwapAssignmentCodePrefixRule("REF"),
            SwapAssignmentLabelLengthRule(16),
            SwapAssignmentRequiredExternalRefRule("swap assignment needs a value"),
        ),
    )
}
