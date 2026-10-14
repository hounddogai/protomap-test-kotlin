package com.acme.transport.lastmile.locker.preference

/** A rule a locker exception must pass. Check returns its problems, or none. */
interface LockerExceptionRule {
    fun check(item: LockerException): MutableList<String>
}

data class LockerExceptionCodePrefixRule(var prefix: String) : LockerExceptionRule {
    override fun check(item: LockerException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerExceptionCodeLengthRule(var maxLength: Int) : LockerExceptionRule {
    override fun check(item: LockerException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerExceptionStopCountRangeRule(var min: Int, var max: Int) : LockerExceptionRule {
    override fun check(item: LockerException): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerExceptionCarrierLengthRule(var maxLength: Int) : LockerExceptionRule {
    override fun check(item: LockerException): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerExceptionRequiredCarrierRule(var message: String) : LockerExceptionRule {
    override fun check(item: LockerException): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker exception. */
data class LockerExceptionValidator(var rules: MutableList<LockerExceptionRule>) {
    fun validate(item: LockerException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerException>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerExceptionValidator(): LockerExceptionValidator {
    return LockerExceptionValidator(
        mutableListOf<LockerExceptionRule>(
            LockerExceptionCodePrefixRule("OPS"),
            LockerExceptionCodeLengthRule(16),
            LockerExceptionStopCountRangeRule(32, 120),
            LockerExceptionCarrierLengthRule(120),
            LockerExceptionRequiredCarrierRule("locker exception needs an entry"),
        ),
    )
}
