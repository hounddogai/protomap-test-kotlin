package com.acme.transport.safety.violation.entry

/** A rule a violation entry must pass. Check returns its problems, or none. */
interface ViolationEntryRule {
    fun check(item: ViolationEntry): MutableList<String>
}

data class ViolationEntryChecksumLengthRule(var maxLength: Int) : ViolationEntryRule {
    override fun check(item: ViolationEntry): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationEntryQuantityRangeRule(var min: Int, var max: Int) : ViolationEntryRule {
    override fun check(item: ViolationEntry): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationEntryCodePrefixRule(var prefix: String) : ViolationEntryRule {
    override fun check(item: ViolationEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationEntryRequiredNameRule(var message: String) : ViolationEntryRule {
    override fun check(item: ViolationEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationEntryLabelLengthRule(var maxLength: Int) : ViolationEntryRule {
    override fun check(item: ViolationEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation entry. */
data class ViolationEntryValidator(var rules: MutableList<ViolationEntryRule>) {
    fun validate(item: ViolationEntry): MutableList<String> {
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

    fun requireValid(item: ViolationEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationEntryValidator(): ViolationEntryValidator {
    return ViolationEntryValidator(
        mutableListOf<ViolationEntryRule>(
            ViolationEntryChecksumLengthRule(64),
            ViolationEntryQuantityRangeRule(1000, 1000),
            ViolationEntryCodePrefixRule("ACM"),
            ViolationEntryRequiredNameRule("violation entry needs a value"),
            ViolationEntryLabelLengthRule(80),
        ),
    )
}
