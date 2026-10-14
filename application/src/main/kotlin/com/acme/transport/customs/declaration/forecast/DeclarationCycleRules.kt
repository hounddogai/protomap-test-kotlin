package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.text.normalize

/** A rule a declaration cycle must pass. Check returns its problems, or none. */
interface DeclarationCycleRule {
    fun check(item: DeclarationCycle): MutableList<String>
}

data class DeclarationCycleCapacityRangeRule(var min: Int, var max: Int) : DeclarationCycleRule {
    override fun check(item: DeclarationCycle): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class DeclarationCycleRequiredNameRule(var message: String) : DeclarationCycleRule {
    override fun check(item: DeclarationCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationCycleOdometerRangeRule(var min: Int, var max: Int) : DeclarationCycleRule {
    override fun check(item: DeclarationCycle): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationCycleNameLengthRule(var maxLength: Int) : DeclarationCycleRule {
    override fun check(item: DeclarationCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationCycleAllowedNameRule(var allowed: MutableList<String>) : DeclarationCycleRule {
    override fun check(item: DeclarationCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationCycleCodePrefixRule(var prefix: String) : DeclarationCycleRule {
    override fun check(item: DeclarationCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration cycle. */
data class DeclarationCycleValidator(var rules: MutableList<DeclarationCycleRule>) {
    fun validate(item: DeclarationCycle): MutableList<String> {
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

    fun requireValid(item: DeclarationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationCycleValidator(): DeclarationCycleValidator {
    return DeclarationCycleValidator(
        mutableListOf<DeclarationCycleRule>(
            DeclarationCycleCapacityRangeRule(64, 86400),
            DeclarationCycleRequiredNameRule("declaration cycle needs this field"),
            DeclarationCycleOdometerRangeRule(16, 1000),
            DeclarationCycleNameLengthRule(16),
            DeclarationCycleAllowedNameRule(mutableListOf<String>("regional", "scheduled")),
            DeclarationCycleCodePrefixRule("OPS"),
        ),
    )
}
