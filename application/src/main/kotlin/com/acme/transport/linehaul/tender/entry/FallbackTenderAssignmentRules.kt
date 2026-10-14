package com.acme.transport.linehaul.tender.entry

/** A rule a fallback tender assignment must pass. Check returns its problems, or none. */
interface FallbackTenderAssignmentRule {
    fun check(item: FallbackTenderAssignment): MutableList<String>
}

data class FallbackTenderAssignmentLimitRangeRule(var min: Int, var max: Int) : FallbackTenderAssignmentRule {
    override fun check(item: FallbackTenderAssignment): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderAssignmentCodeLengthRule(var maxLength: Int) : FallbackTenderAssignmentRule {
    override fun check(item: FallbackTenderAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderAssignmentRequiredCodeRule(var message: String) : FallbackTenderAssignmentRule {
    override fun check(item: FallbackTenderAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderAssignmentRequiredLabelRule(var message: String) : FallbackTenderAssignmentRule {
    override fun check(item: FallbackTenderAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderAssignmentLabelLengthRule(var maxLength: Int) : FallbackTenderAssignmentRule {
    override fun check(item: FallbackTenderAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderAssignmentCodePrefixRule(var prefix: String) : FallbackTenderAssignmentRule {
    override fun check(item: FallbackTenderAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback tender assignment. */
data class FallbackTenderAssignmentValidator(var rules: MutableList<FallbackTenderAssignmentRule>) {
    fun validate(item: FallbackTenderAssignment): MutableList<String> {
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

    fun requireValid(item: FallbackTenderAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTenderAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTenderAssignmentValidator(): FallbackTenderAssignmentValidator {
    return FallbackTenderAssignmentValidator(
        mutableListOf<FallbackTenderAssignmentRule>(
            FallbackTenderAssignmentLimitRangeRule(255, 255),
            FallbackTenderAssignmentCodeLengthRule(3600),
            FallbackTenderAssignmentRequiredCodeRule("fallback tender assignment needs an entry"),
            FallbackTenderAssignmentRequiredLabelRule("fallback tender assignment needs this field"),
            FallbackTenderAssignmentLabelLengthRule(3600),
            FallbackTenderAssignmentCodePrefixRule("REF"),
        ),
    )
}
