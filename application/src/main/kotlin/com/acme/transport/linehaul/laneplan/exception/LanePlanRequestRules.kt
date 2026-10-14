package com.acme.transport.linehaul.laneplan.exception

/** A rule a lane plan request must pass. Check returns its problems, or none. */
interface LanePlanRequestRule {
    fun check(item: LanePlanRequest): MutableList<String>
}

data class LanePlanRequestLimitRangeRule(var min: Int, var max: Int) : LanePlanRequestRule {
    override fun check(item: LanePlanRequest): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LanePlanRequestCodeLengthRule(var maxLength: Int) : LanePlanRequestRule {
    override fun check(item: LanePlanRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanRequestLabelLengthRule(var maxLength: Int) : LanePlanRequestRule {
    override fun check(item: LanePlanRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanRequestCodePrefixRule(var prefix: String) : LanePlanRequestRule {
    override fun check(item: LanePlanRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanRequestRateRangeRule(var min: Double, var max: Double) : LanePlanRequestRule {
    override fun check(item: LanePlanRequest): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lane plan request. */
data class LanePlanRequestValidator(var rules: MutableList<LanePlanRequestRule>) {
    fun validate(item: LanePlanRequest): MutableList<String> {
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

    fun requireValid(item: LanePlanRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanRequestValidator(): LanePlanRequestValidator {
    return LanePlanRequestValidator(
        mutableListOf<LanePlanRequestRule>(
            LanePlanRequestLimitRangeRule(64, 80),
            LanePlanRequestCodeLengthRule(1000),
            LanePlanRequestLabelLengthRule(1000),
            LanePlanRequestCodePrefixRule("ACM"),
            LanePlanRequestRateRangeRule(4.1, 2462.9),
        ),
    )
}
