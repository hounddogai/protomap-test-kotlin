package com.acme.transport.emissions.idling.idlingcheckpoint

/** A rule a default idling rollup must pass. Check returns its problems, or none. */
interface DefaultIdlingRollupRule {
    fun check(item: DefaultIdlingRollup): MutableList<String>
}

data class DefaultIdlingRollupRequiredChecksumRule(var message: String) : DefaultIdlingRollupRule {
    override fun check(item: DefaultIdlingRollup): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingRollupRetriesRangeRule(var min: Int, var max: Int) : DefaultIdlingRollupRule {
    override fun check(item: DefaultIdlingRollup): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingRollupEtaMinutesRangeRule(var min: Int, var max: Int) : DefaultIdlingRollupRule {
    override fun check(item: DefaultIdlingRollup): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class DefaultIdlingRollupWindowDaysRangeRule(var min: Int, var max: Int) : DefaultIdlingRollupRule {
    override fun check(item: DefaultIdlingRollup): MutableList<String> {
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

data class DefaultIdlingRollupMinimumRangeRule(var min: Double, var max: Double) : DefaultIdlingRollupRule {
    override fun check(item: DefaultIdlingRollup): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default idling rollup. */
data class DefaultIdlingRollupValidator(var rules: MutableList<DefaultIdlingRollupRule>) {
    fun validate(item: DefaultIdlingRollup): MutableList<String> {
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

    fun requireValid(item: DefaultIdlingRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultIdlingRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultIdlingRollupValidator(): DefaultIdlingRollupValidator {
    return DefaultIdlingRollupValidator(
        mutableListOf<DefaultIdlingRollupRule>(
            DefaultIdlingRollupRequiredChecksumRule("default idling rollup needs this field"),
            DefaultIdlingRollupRetriesRangeRule(16, 86400),
            DefaultIdlingRollupEtaMinutesRangeRule(16, 255),
            DefaultIdlingRollupWindowDaysRangeRule(32, 255),
            DefaultIdlingRollupMinimumRangeRule(3.4, 2422.4),
        ),
    )
}
