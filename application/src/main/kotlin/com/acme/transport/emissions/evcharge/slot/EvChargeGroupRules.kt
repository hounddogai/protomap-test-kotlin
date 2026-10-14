package com.acme.transport.emissions.evcharge.slot

/** A rule a ev charge group must pass. Check returns its problems, or none. */
interface EvChargeGroupRule {
    fun check(item: EvChargeGroup): MutableList<String>
}

data class EvChargeGroupRequiredExternalRefRule(var message: String) : EvChargeGroupRule {
    override fun check(item: EvChargeGroup): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeGroupQuantityRangeRule(var min: Int, var max: Int) : EvChargeGroupRule {
    override fun check(item: EvChargeGroup): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeGroupCodeLengthRule(var maxLength: Int) : EvChargeGroupRule {
    override fun check(item: EvChargeGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge group. */
data class EvChargeGroupValidator(var rules: MutableList<EvChargeGroupRule>) {
    fun validate(item: EvChargeGroup): MutableList<String> {
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

    fun requireValid(item: EvChargeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeGroupValidator(): EvChargeGroupValidator {
    return EvChargeGroupValidator(
        mutableListOf<EvChargeGroupRule>(
            EvChargeGroupRequiredExternalRefRule("ev charge group needs this field"),
            EvChargeGroupQuantityRangeRule(120, 255),
            EvChargeGroupCodeLengthRule(255),
        ),
    )
}
