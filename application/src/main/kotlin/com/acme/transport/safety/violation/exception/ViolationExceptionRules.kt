package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.normalize

/** A rule a violation exception must pass. Check returns its problems, or none. */
interface ViolationExceptionRule {
    fun check(item: ViolationException): MutableList<String>
}

data class ViolationExceptionCarrierLengthRule(var maxLength: Int) : ViolationExceptionRule {
    override fun check(item: ViolationException): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationExceptionRequiredDescriptionRule(var message: String) : ViolationExceptionRule {
    override fun check(item: ViolationException): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationExceptionRequiredCarrierRule(var message: String) : ViolationExceptionRule {
    override fun check(item: ViolationException): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationExceptionAllowedDescriptionRule(var allowed: MutableList<String>) : ViolationExceptionRule {
    override fun check(item: ViolationException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ViolationExceptionBatchSizeRangeRule(var min: Int, var max: Int) : ViolationExceptionRule {
    override fun check(item: ViolationException): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a violation exception. */
data class ViolationExceptionValidator(var rules: MutableList<ViolationExceptionRule>) {
    fun validate(item: ViolationException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationException>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationExceptionValidator(): ViolationExceptionValidator {
    return ViolationExceptionValidator(
        mutableListOf<ViolationExceptionRule>(
            ViolationExceptionCarrierLengthRule(1000),
            ViolationExceptionRequiredDescriptionRule("violation exception needs an entry"),
            ViolationExceptionRequiredCarrierRule("violation exception needs this field"),
            ViolationExceptionAllowedDescriptionRule(mutableListOf<String>("primary")),
            ViolationExceptionBatchSizeRangeRule(16, 255),
        ),
    )
}
