package com.acme.transport.customs.hold.holdslot

/** A rule a pending hold window must pass. Check returns its problems, or none. */
interface PendingHoldWindowRule {
    fun check(item: PendingHoldWindow): MutableList<String>
}

data class PendingHoldWindowLabelLengthRule(var maxLength: Int) : PendingHoldWindowRule {
    override fun check(item: PendingHoldWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldWindowContactEmailLengthRule(var maxLength: Int) : PendingHoldWindowRule {
    override fun check(item: PendingHoldWindow): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldWindowCodePrefixRule(var prefix: String) : PendingHoldWindowRule {
    override fun check(item: PendingHoldWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending hold window. */
data class PendingHoldWindowValidator(var rules: MutableList<PendingHoldWindowRule>) {
    fun validate(item: PendingHoldWindow): MutableList<String> {
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

    fun requireValid(item: PendingHoldWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingHoldWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingHoldWindowValidator(): PendingHoldWindowValidator {
    return PendingHoldWindowValidator(
        mutableListOf<PendingHoldWindowRule>(
            PendingHoldWindowLabelLengthRule(64),
            PendingHoldWindowContactEmailLengthRule(1000),
            PendingHoldWindowCodePrefixRule("OPS"),
        ),
    )
}
