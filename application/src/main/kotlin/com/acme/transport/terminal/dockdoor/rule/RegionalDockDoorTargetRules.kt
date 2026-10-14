package com.acme.transport.terminal.dockdoor.rule

/** A rule a regional dock door target must pass. Check returns its problems, or none. */
interface RegionalDockDoorTargetRule {
    fun check(item: RegionalDockDoorTarget): MutableList<String>
}

data class RegionalDockDoorTargetRequiredCodeRule(var message: String) : RegionalDockDoorTargetRule {
    override fun check(item: RegionalDockDoorTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorTargetCodePrefixRule(var prefix: String) : RegionalDockDoorTargetRule {
    override fun check(item: RegionalDockDoorTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorTargetTitleLengthRule(var maxLength: Int) : RegionalDockDoorTargetRule {
    override fun check(item: RegionalDockDoorTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorTargetMarginRangeRule(
    var min: Double,
    var max: Double,
) : RegionalDockDoorTargetRule {
    override fun check(item: RegionalDockDoorTarget): MutableList<String> {
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

data class RegionalDockDoorTargetCreatedAtRangeRule(var min: Int, var max: Int) : RegionalDockDoorTargetRule {
    override fun check(item: RegionalDockDoorTarget): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional dock door target. */
data class RegionalDockDoorTargetValidator(var rules: MutableList<RegionalDockDoorTargetRule>) {
    fun validate(item: RegionalDockDoorTarget): MutableList<String> {
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

    fun requireValid(item: RegionalDockDoorTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDockDoorTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDockDoorTargetValidator(): RegionalDockDoorTargetValidator {
    return RegionalDockDoorTargetValidator(
        mutableListOf<RegionalDockDoorTargetRule>(
            RegionalDockDoorTargetRequiredCodeRule("regional dock door target needs an entry"),
            RegionalDockDoorTargetCodePrefixRule("OPS"),
            RegionalDockDoorTargetTitleLengthRule(1000),
            RegionalDockDoorTargetMarginRangeRule(3.6, 1203.3),
            RegionalDockDoorTargetCreatedAtRangeRule(80, 3600),
        ),
    )
}
