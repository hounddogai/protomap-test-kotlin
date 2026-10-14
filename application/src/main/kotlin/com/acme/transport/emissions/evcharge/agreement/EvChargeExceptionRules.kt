package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.text.normalize

/** A rule a ev charge exception must pass. Check returns its problems, or none. */
interface EvChargeExceptionRule {
    fun check(item: EvChargeException): MutableList<String>
}

data class EvChargeExceptionRequiredLabelRule(var message: String) : EvChargeExceptionRule {
    override fun check(item: EvChargeException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeExceptionAllowedLabelRule(var allowed: MutableList<String>) : EvChargeExceptionRule {
    override fun check(item: EvChargeException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeExceptionRequiredCodeRule(var message: String) : EvChargeExceptionRule {
    override fun check(item: EvChargeException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeExceptionLabelLengthRule(var maxLength: Int) : EvChargeExceptionRule {
    override fun check(item: EvChargeException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeExceptionQuantityRangeRule(var min: Int, var max: Int) : EvChargeExceptionRule {
    override fun check(item: EvChargeException): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeExceptionAmountRangeRule(var min: Double, var max: Double) : EvChargeExceptionRule {
    override fun check(item: EvChargeException): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge exception. */
data class EvChargeExceptionValidator(var rules: MutableList<EvChargeExceptionRule>) {
    fun validate(item: EvChargeException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeException>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeExceptionValidator(): EvChargeExceptionValidator {
    return EvChargeExceptionValidator(
        mutableListOf<EvChargeExceptionRule>(
            EvChargeExceptionRequiredLabelRule("ev charge exception needs this field"),
            EvChargeExceptionAllowedLabelRule(mutableListOf<String>("tiered", "regional", "regional")),
            EvChargeExceptionRequiredCodeRule("ev charge exception needs this field"),
            EvChargeExceptionLabelLengthRule(86400),
            EvChargeExceptionQuantityRangeRule(32, 3600),
            EvChargeExceptionAmountRangeRule(5.4, 3032.0),
        ),
    )
}
