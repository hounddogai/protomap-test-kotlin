package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.kit.text.normalize

/** A rule a manual tire cycle must pass. Check returns its problems, or none. */
interface ManualTireCycleRule {
    fun check(item: ManualTireCycle): MutableList<String>
}

data class ManualTireCycleDiscountRangeRule(var min: Double, var max: Double) : ManualTireCycleRule {
    override fun check(item: ManualTireCycle): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class ManualTireCycleAllowedReviewerRule(var allowed: MutableList<String>) : ManualTireCycleRule {
    override fun check(item: ManualTireCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ManualTireCycleRequiredCodeRule(var message: String) : ManualTireCycleRule {
    override fun check(item: ManualTireCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTireCycleLaneLengthRule(var maxLength: Int) : ManualTireCycleRule {
    override fun check(item: ManualTireCycle): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTireCycleCodeLengthRule(var maxLength: Int) : ManualTireCycleRule {
    override fun check(item: ManualTireCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTireCycleRequiredNameRule(var message: String) : ManualTireCycleRule {
    override fun check(item: ManualTireCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tire cycle. */
data class ManualTireCycleValidator(var rules: MutableList<ManualTireCycleRule>) {
    fun validate(item: ManualTireCycle): MutableList<String> {
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

    fun requireValid(item: ManualTireCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTireCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTireCycleValidator(): ManualTireCycleValidator {
    return ManualTireCycleValidator(
        mutableListOf<ManualTireCycleRule>(
            ManualTireCycleDiscountRangeRule(0.3, 3157.2),
            ManualTireCycleAllowedReviewerRule(mutableListOf<String>("strict", "strict")),
            ManualTireCycleRequiredCodeRule("manual tire cycle needs an entry"),
            ManualTireCycleLaneLengthRule(86400),
            ManualTireCycleCodeLengthRule(32),
            ManualTireCycleRequiredNameRule("manual tire cycle needs an entry"),
        ),
    )
}
