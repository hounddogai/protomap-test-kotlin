package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.text.normalize

/** A rule a strict dock door tier must pass. Check returns its problems, or none. */
interface StrictDockDoorTierRule {
    fun check(item: StrictDockDoorTier): MutableList<String>
}

data class StrictDockDoorTierRequiredCodeRule(var message: String) : StrictDockDoorTierRule {
    override fun check(item: StrictDockDoorTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorTierAttemptsRangeRule(var min: Int, var max: Int) : StrictDockDoorTierRule {
    override fun check(item: StrictDockDoorTier): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class StrictDockDoorTierCodePrefixRule(var prefix: String) : StrictDockDoorTierRule {
    override fun check(item: StrictDockDoorTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorTierCategoryLengthRule(var maxLength: Int) : StrictDockDoorTierRule {
    override fun check(item: StrictDockDoorTier): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorTierDockLengthRule(var maxLength: Int) : StrictDockDoorTierRule {
    override fun check(item: StrictDockDoorTier): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorTierAllowedDockRule(var allowed: MutableList<String>) : StrictDockDoorTierRule {
    override fun check(item: StrictDockDoorTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dock door tier. */
data class StrictDockDoorTierValidator(var rules: MutableList<StrictDockDoorTierRule>) {
    fun validate(item: StrictDockDoorTier): MutableList<String> {
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

    fun requireValid(item: StrictDockDoorTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDockDoorTier>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDockDoorTierValidator(): StrictDockDoorTierValidator {
    return StrictDockDoorTierValidator(
        mutableListOf<StrictDockDoorTierRule>(
            StrictDockDoorTierRequiredCodeRule("strict dock door tier needs an entry"),
            StrictDockDoorTierAttemptsRangeRule(32, 255),
            StrictDockDoorTierCodePrefixRule("ACM"),
            StrictDockDoorTierCategoryLengthRule(86400),
            StrictDockDoorTierDockLengthRule(80),
            StrictDockDoorTierAllowedDockRule(mutableListOf<String>("scheduled", "fallback", "pending")),
        ),
    )
}
