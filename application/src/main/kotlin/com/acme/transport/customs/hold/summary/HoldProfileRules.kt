package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.normalize

/** A rule a hold profile must pass. Check returns its problems, or none. */
interface HoldProfileRule {
    fun check(item: HoldProfile): MutableList<String>
}

data class HoldProfileAllowedLabelRule(var allowed: MutableList<String>) : HoldProfileRule {
    override fun check(item: HoldProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class HoldProfileRequiredShiftCodeRule(var message: String) : HoldProfileRule {
    override fun check(item: HoldProfile): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldProfileRateRangeRule(var min: Double, var max: Double) : HoldProfileRule {
    override fun check(item: HoldProfile): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldProfileLabelLengthRule(var maxLength: Int) : HoldProfileRule {
    override fun check(item: HoldProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldProfileShiftCodeLengthRule(var maxLength: Int) : HoldProfileRule {
    override fun check(item: HoldProfile): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldProfileRequiredLabelRule(var message: String) : HoldProfileRule {
    override fun check(item: HoldProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold profile. */
data class HoldProfileValidator(var rules: MutableList<HoldProfileRule>) {
    fun validate(item: HoldProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldProfileValidator(): HoldProfileValidator {
    return HoldProfileValidator(
        mutableListOf<HoldProfileRule>(
            HoldProfileAllowedLabelRule(mutableListOf<String>()),
            HoldProfileRequiredShiftCodeRule("hold profile needs an entry"),
            HoldProfileRateRangeRule(9.3, 553.9),
            HoldProfileLabelLengthRule(80),
            HoldProfileShiftCodeLengthRule(32),
            HoldProfileRequiredLabelRule("hold profile needs an entry"),
        ),
    )
}
