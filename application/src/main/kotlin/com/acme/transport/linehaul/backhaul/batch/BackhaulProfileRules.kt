package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.text.normalize

/** A rule a backhaul profile must pass. Check returns its problems, or none. */
interface BackhaulProfileRule {
    fun check(item: BackhaulProfile): MutableList<String>
}

data class BackhaulProfileRequiredLabelRule(var message: String) : BackhaulProfileRule {
    override fun check(item: BackhaulProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProfileCodePrefixRule(var prefix: String) : BackhaulProfileRule {
    override fun check(item: BackhaulProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProfileLimitRangeRule(var min: Int, var max: Int) : BackhaulProfileRule {
    override fun check(item: BackhaulProfile): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProfileLabelLengthRule(var maxLength: Int) : BackhaulProfileRule {
    override fun check(item: BackhaulProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProfileAllowedCodeRule(var allowed: MutableList<String>) : BackhaulProfileRule {
    override fun check(item: BackhaulProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProfileRequiredCategoryRule(var message: String) : BackhaulProfileRule {
    override fun check(item: BackhaulProfile): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul profile. */
data class BackhaulProfileValidator(var rules: MutableList<BackhaulProfileRule>) {
    fun validate(item: BackhaulProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulProfileValidator(): BackhaulProfileValidator {
    return BackhaulProfileValidator(
        mutableListOf<BackhaulProfileRule>(
            BackhaulProfileRequiredLabelRule("backhaul profile needs this field"),
            BackhaulProfileCodePrefixRule("REF"),
            BackhaulProfileLimitRangeRule(16, 3600),
            BackhaulProfileLabelLengthRule(32),
            BackhaulProfileAllowedCodeRule(mutableListOf<String>("legacy", "primary")),
            BackhaulProfileRequiredCategoryRule("backhaul profile needs this field"),
        ),
    )
}
