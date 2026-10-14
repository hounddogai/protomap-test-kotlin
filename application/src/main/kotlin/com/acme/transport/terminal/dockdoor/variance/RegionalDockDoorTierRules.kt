package com.acme.transport.terminal.dockdoor.variance

/** A rule a regional dock door tier must pass. Check returns its problems, or none. */
interface RegionalDockDoorTierRule {
    fun check(item: RegionalDockDoorTier): MutableList<String>
}

data class RegionalDockDoorTierRequiredCodeRule(var message: String) : RegionalDockDoorTierRule {
    override fun check(item: RegionalDockDoorTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorTierCodePrefixRule(var prefix: String) : RegionalDockDoorTierRule {
    override fun check(item: RegionalDockDoorTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorTierLabelLengthRule(var maxLength: Int) : RegionalDockDoorTierRule {
    override fun check(item: RegionalDockDoorTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorTierCodeLengthRule(var maxLength: Int) : RegionalDockDoorTierRule {
    override fun check(item: RegionalDockDoorTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorTierRequiredSummaryRule(var message: String) : RegionalDockDoorTierRule {
    override fun check(item: RegionalDockDoorTier): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorTierMarginRangeRule(var min: Double, var max: Double) : RegionalDockDoorTierRule {
    override fun check(item: RegionalDockDoorTier): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional dock door tier. */
data class RegionalDockDoorTierValidator(var rules: MutableList<RegionalDockDoorTierRule>) {
    fun validate(item: RegionalDockDoorTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDockDoorTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDockDoorTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDockDoorTierValidator(): RegionalDockDoorTierValidator {
    return RegionalDockDoorTierValidator(
        mutableListOf<RegionalDockDoorTierRule>(
            RegionalDockDoorTierRequiredCodeRule("regional dock door tier needs a value"),
            RegionalDockDoorTierCodePrefixRule("REF"),
            RegionalDockDoorTierLabelLengthRule(32),
            RegionalDockDoorTierCodeLengthRule(3600),
            RegionalDockDoorTierRequiredSummaryRule("regional dock door tier needs a value"),
            RegionalDockDoorTierMarginRangeRule(7.9, 661.8),
        ),
    )
}
