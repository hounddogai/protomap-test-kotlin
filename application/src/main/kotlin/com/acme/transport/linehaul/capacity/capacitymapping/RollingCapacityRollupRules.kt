package com.acme.transport.linehaul.capacity.capacitymapping

/** A rule a rolling capacity rollup must pass. Check returns its problems, or none. */
interface RollingCapacityRollupRule {
    fun check(item: RollingCapacityRollup): MutableList<String>
}

data class RollingCapacityRollupExternalRefLengthRule(var maxLength: Int) : RollingCapacityRollupRule {
    override fun check(item: RollingCapacityRollup): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCapacityRollupRequiredLabelRule(var message: String) : RollingCapacityRollupRule {
    override fun check(item: RollingCapacityRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCapacityRollupRequiredCodeRule(var message: String) : RollingCapacityRollupRule {
    override fun check(item: RollingCapacityRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCapacityRollupCodePrefixRule(var prefix: String) : RollingCapacityRollupRule {
    override fun check(item: RollingCapacityRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingCapacityRollupWindowDaysRangeRule(var min: Int, var max: Int) : RollingCapacityRollupRule {
    override fun check(item: RollingCapacityRollup): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class RollingCapacityRollupRequiredExternalRefRule(var message: String) : RollingCapacityRollupRule {
    override fun check(item: RollingCapacityRollup): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling capacity rollup. */
data class RollingCapacityRollupValidator(var rules: MutableList<RollingCapacityRollupRule>) {
    fun validate(item: RollingCapacityRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCapacityRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCapacityRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCapacityRollupValidator(): RollingCapacityRollupValidator {
    return RollingCapacityRollupValidator(
        mutableListOf<RollingCapacityRollupRule>(
            RollingCapacityRollupExternalRefLengthRule(1000),
            RollingCapacityRollupRequiredLabelRule("rolling capacity rollup needs this field"),
            RollingCapacityRollupRequiredCodeRule("rolling capacity rollup needs an entry"),
            RollingCapacityRollupCodePrefixRule("Q"),
            RollingCapacityRollupWindowDaysRangeRule(86400, 86400),
            RollingCapacityRollupRequiredExternalRefRule("rolling capacity rollup needs an entry"),
        ),
    )
}
