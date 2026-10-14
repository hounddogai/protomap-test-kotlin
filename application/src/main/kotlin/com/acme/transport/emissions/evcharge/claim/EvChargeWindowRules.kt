package com.acme.transport.emissions.evcharge.claim

/** A rule a ev charge window must pass. Check returns its problems, or none. */
interface EvChargeWindowRule {
    fun check(item: EvChargeWindow): MutableList<String>
}

data class EvChargeWindowContactEmailLengthRule(var maxLength: Int) : EvChargeWindowRule {
    override fun check(item: EvChargeWindow): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeWindowCodePrefixRule(var prefix: String) : EvChargeWindowRule {
    override fun check(item: EvChargeWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeWindowWeightRangeRule(var min: Double, var max: Double) : EvChargeWindowRule {
    override fun check(item: EvChargeWindow): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class EvChargeWindowPhoneLengthRule(var maxLength: Int) : EvChargeWindowRule {
    override fun check(item: EvChargeWindow): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge window. */
data class EvChargeWindowValidator(var rules: MutableList<EvChargeWindowRule>) {
    fun validate(item: EvChargeWindow): MutableList<String> {
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

    fun requireValid(item: EvChargeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeWindowValidator(): EvChargeWindowValidator {
    return EvChargeWindowValidator(
        mutableListOf<EvChargeWindowRule>(
            EvChargeWindowContactEmailLengthRule(32),
            EvChargeWindowCodePrefixRule("Q"),
            EvChargeWindowWeightRangeRule(4.2, 3032.3),
            EvChargeWindowPhoneLengthRule(80),
        ),
    )
}
