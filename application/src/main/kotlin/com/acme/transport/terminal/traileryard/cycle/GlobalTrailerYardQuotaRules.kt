package com.acme.transport.terminal.traileryard.cycle

/** A rule a global trailer yard quota must pass. Check returns its problems, or none. */
interface GlobalTrailerYardQuotaRule {
    fun check(item: GlobalTrailerYardQuota): MutableList<String>
}

data class GlobalTrailerYardQuotaAttemptsRangeRule(var min: Int, var max: Int) : GlobalTrailerYardQuotaRule {
    override fun check(item: GlobalTrailerYardQuota): MutableList<String> {
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

data class GlobalTrailerYardQuotaRegionLengthRule(var maxLength: Int) : GlobalTrailerYardQuotaRule {
    override fun check(item: GlobalTrailerYardQuota): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardQuotaExpiresAtRangeRule(var min: Int, var max: Int) : GlobalTrailerYardQuotaRule {
    override fun check(item: GlobalTrailerYardQuota): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardQuotaCostRangeRule(var min: Double, var max: Double) : GlobalTrailerYardQuotaRule {
    override fun check(item: GlobalTrailerYardQuota): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class GlobalTrailerYardQuotaVersionRangeRule(var min: Int, var max: Int) : GlobalTrailerYardQuotaRule {
    override fun check(item: GlobalTrailerYardQuota): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global trailer yard quota. */
data class GlobalTrailerYardQuotaValidator(var rules: MutableList<GlobalTrailerYardQuotaRule>) {
    fun validate(item: GlobalTrailerYardQuota): MutableList<String> {
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

    fun requireValid(item: GlobalTrailerYardQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerYardQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerYardQuotaValidator(): GlobalTrailerYardQuotaValidator {
    return GlobalTrailerYardQuotaValidator(
        mutableListOf<GlobalTrailerYardQuotaRule>(
            GlobalTrailerYardQuotaAttemptsRangeRule(1000, 86400),
            GlobalTrailerYardQuotaRegionLengthRule(3600),
            GlobalTrailerYardQuotaExpiresAtRangeRule(64, 3600),
            GlobalTrailerYardQuotaCostRangeRule(9.4, 3955.1),
            GlobalTrailerYardQuotaVersionRangeRule(120, 1000),
        ),
    )
}
