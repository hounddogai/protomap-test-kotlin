package com.acme.transport.lastmile.locker.quota

import com.acme.transport.kit.text.normalize

/** A rule a locker threshold must pass. Check returns its problems, or none. */
interface LockerThresholdRule {
    fun check(item: LockerThreshold): MutableList<String>
}

data class LockerThresholdAllowedCodeRule(var allowed: MutableList<String>) : LockerThresholdRule {
    override fun check(item: LockerThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class LockerThresholdCodeLengthRule(var maxLength: Int) : LockerThresholdRule {
    override fun check(item: LockerThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerThresholdRequiredNameRule(var message: String) : LockerThresholdRule {
    override fun check(item: LockerThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerThresholdWeightRangeRule(var min: Double, var max: Double) : LockerThresholdRule {
    override fun check(item: LockerThreshold): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker threshold. */
data class LockerThresholdValidator(var rules: MutableList<LockerThresholdRule>) {
    fun validate(item: LockerThreshold): MutableList<String> {
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

    fun requireValid(item: LockerThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerThresholdValidator(): LockerThresholdValidator {
    return LockerThresholdValidator(
        mutableListOf<LockerThresholdRule>(
            LockerThresholdAllowedCodeRule(mutableListOf<String>("scheduled", "lenient", "legacy")),
            LockerThresholdCodeLengthRule(86400),
            LockerThresholdRequiredNameRule("locker threshold needs an entry"),
            LockerThresholdWeightRangeRule(9.8, 3701.2),
        ),
    )
}
