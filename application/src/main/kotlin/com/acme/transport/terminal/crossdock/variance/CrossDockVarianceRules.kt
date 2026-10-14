package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.kit.text.normalize

/** A rule a cross dock variance must pass. Check returns its problems, or none. */
interface CrossDockVarianceRule {
    fun check(item: CrossDockVariance): MutableList<String>
}

data class CrossDockVarianceCodePrefixRule(var prefix: String) : CrossDockVarianceRule {
    override fun check(item: CrossDockVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockVarianceRequiredShiftCodeRule(var message: String) : CrossDockVarianceRule {
    override fun check(item: CrossDockVariance): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockVarianceShiftCodeLengthRule(var maxLength: Int) : CrossDockVarianceRule {
    override fun check(item: CrossDockVariance): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockVarianceGraceDaysRangeRule(var min: Int, var max: Int) : CrossDockVarianceRule {
    override fun check(item: CrossDockVariance): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockVarianceAllowedNameRule(var allowed: MutableList<String>) : CrossDockVarianceRule {
    override fun check(item: CrossDockVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockVarianceNameLengthRule(var maxLength: Int) : CrossDockVarianceRule {
    override fun check(item: CrossDockVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock variance. */
data class CrossDockVarianceValidator(var rules: MutableList<CrossDockVarianceRule>) {
    fun validate(item: CrossDockVariance): MutableList<String> {
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

    fun requireValid(item: CrossDockVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockVarianceValidator(): CrossDockVarianceValidator {
    return CrossDockVarianceValidator(
        mutableListOf<CrossDockVarianceRule>(
            CrossDockVarianceCodePrefixRule("OPS"),
            CrossDockVarianceRequiredShiftCodeRule("cross dock variance needs an entry"),
            CrossDockVarianceShiftCodeLengthRule(64),
            CrossDockVarianceGraceDaysRangeRule(80, 255),
            CrossDockVarianceAllowedNameRule(mutableListOf<String>("regional")),
            CrossDockVarianceNameLengthRule(16),
        ),
    )
}
