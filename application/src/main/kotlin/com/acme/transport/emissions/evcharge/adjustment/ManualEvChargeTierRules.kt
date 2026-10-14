package com.acme.transport.emissions.evcharge.adjustment

/** A rule a manual ev charge tier must pass. Check returns its problems, or none. */
interface ManualEvChargeTierRule {
    fun check(item: ManualEvChargeTier): MutableList<String>
}

data class ManualEvChargeTierCostRangeRule(var min: Double, var max: Double) : ManualEvChargeTierRule {
    override fun check(item: ManualEvChargeTier): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class ManualEvChargeTierCodePrefixRule(var prefix: String) : ManualEvChargeTierRule {
    override fun check(item: ManualEvChargeTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeTierCodeLengthRule(var maxLength: Int) : ManualEvChargeTierRule {
    override fun check(item: ManualEvChargeTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeTierAttemptsRangeRule(var min: Int, var max: Int) : ManualEvChargeTierRule {
    override fun check(item: ManualEvChargeTier): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeTierRequiredCodeRule(var message: String) : ManualEvChargeTierRule {
    override fun check(item: ManualEvChargeTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual ev charge tier. */
data class ManualEvChargeTierValidator(var rules: MutableList<ManualEvChargeTierRule>) {
    fun validate(item: ManualEvChargeTier): MutableList<String> {
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

    fun requireValid(item: ManualEvChargeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualEvChargeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultManualEvChargeTierValidator(): ManualEvChargeTierValidator {
    return ManualEvChargeTierValidator(
        mutableListOf<ManualEvChargeTierRule>(
            ManualEvChargeTierCostRangeRule(0.8, 73.9),
            ManualEvChargeTierCodePrefixRule("Q"),
            ManualEvChargeTierCodeLengthRule(64),
            ManualEvChargeTierAttemptsRangeRule(32, 86400),
            ManualEvChargeTierRequiredCodeRule("manual ev charge tier needs this field"),
        ),
    )
}
