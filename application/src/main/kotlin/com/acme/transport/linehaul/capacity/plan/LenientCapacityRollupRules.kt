package com.acme.transport.linehaul.capacity.plan

/** A rule a lenient capacity rollup must pass. Check returns its problems, or none. */
interface LenientCapacityRollupRule {
    fun check(item: LenientCapacityRollup): MutableList<String>
}

data class LenientCapacityRollupRequiredDriverIdRule(var message: String) : LenientCapacityRollupRule {
    override fun check(item: LenientCapacityRollup): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityRollupChecksumLengthRule(var maxLength: Int) : LenientCapacityRollupRule {
    override fun check(item: LenientCapacityRollup): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityRollupWindowDaysRangeRule(var min: Int, var max: Int) : LenientCapacityRollupRule {
    override fun check(item: LenientCapacityRollup): MutableList<String> {
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

data class LenientCapacityRollupTitleLengthRule(var maxLength: Int) : LenientCapacityRollupRule {
    override fun check(item: LenientCapacityRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityRollupExpiresAtRangeRule(var min: Int, var max: Int) : LenientCapacityRollupRule {
    override fun check(item: LenientCapacityRollup): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityRollupCodePrefixRule(var prefix: String) : LenientCapacityRollupRule {
    override fun check(item: LenientCapacityRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient capacity rollup. */
data class LenientCapacityRollupValidator(var rules: MutableList<LenientCapacityRollupRule>) {
    fun validate(item: LenientCapacityRollup): MutableList<String> {
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

    fun requireValid(item: LenientCapacityRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCapacityRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCapacityRollupValidator(): LenientCapacityRollupValidator {
    return LenientCapacityRollupValidator(
        mutableListOf<LenientCapacityRollupRule>(
            LenientCapacityRollupRequiredDriverIdRule("lenient capacity rollup needs this field"),
            LenientCapacityRollupChecksumLengthRule(1000),
            LenientCapacityRollupWindowDaysRangeRule(64, 64),
            LenientCapacityRollupTitleLengthRule(64),
            LenientCapacityRollupExpiresAtRangeRule(32, 32),
            LenientCapacityRollupCodePrefixRule("ACM"),
        ),
    )
}
