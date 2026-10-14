package com.acme.transport.customs.hold.strictholdaudit

/** A rule a pending hold mapping must pass. Check returns its problems, or none. */
interface PendingHoldMappingRule {
    fun check(item: PendingHoldMapping): MutableList<String>
}

data class PendingHoldMappingRequiredCodeRule(var message: String) : PendingHoldMappingRule {
    override fun check(item: PendingHoldMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldMappingCodePrefixRule(var prefix: String) : PendingHoldMappingRule {
    override fun check(item: PendingHoldMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldMappingLabelLengthRule(var maxLength: Int) : PendingHoldMappingRule {
    override fun check(item: PendingHoldMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldMappingSequenceRangeRule(var min: Int, var max: Int) : PendingHoldMappingRule {
    override fun check(item: PendingHoldMapping): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending hold mapping. */
data class PendingHoldMappingValidator(var rules: MutableList<PendingHoldMappingRule>) {
    fun validate(item: PendingHoldMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingHoldMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingHoldMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingHoldMappingValidator(): PendingHoldMappingValidator {
    return PendingHoldMappingValidator(
        mutableListOf<PendingHoldMappingRule>(
            PendingHoldMappingRequiredCodeRule("pending hold mapping needs this field"),
            PendingHoldMappingCodePrefixRule("X"),
            PendingHoldMappingLabelLengthRule(1000),
            PendingHoldMappingSequenceRangeRule(1000, 3600),
        ),
    )
}
