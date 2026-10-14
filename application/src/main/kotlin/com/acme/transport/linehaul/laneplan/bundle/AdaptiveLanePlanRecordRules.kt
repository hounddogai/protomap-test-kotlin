package com.acme.transport.linehaul.laneplan.bundle

/** A rule a adaptive lane plan record must pass. Check returns its problems, or none. */
interface AdaptiveLanePlanRecordRule {
    fun check(item: AdaptiveLanePlanRecord): MutableList<String>
}

data class AdaptiveLanePlanRecordEndsAtRangeRule(var min: Int, var max: Int) : AdaptiveLanePlanRecordRule {
    override fun check(item: AdaptiveLanePlanRecord): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanRecordRequiredExternalRefRule(var message: String) : AdaptiveLanePlanRecordRule {
    override fun check(item: AdaptiveLanePlanRecord): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanRecordCodePrefixRule(var prefix: String) : AdaptiveLanePlanRecordRule {
    override fun check(item: AdaptiveLanePlanRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanRecordRequiredNameRule(var message: String) : AdaptiveLanePlanRecordRule {
    override fun check(item: AdaptiveLanePlanRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanRecordMarginRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveLanePlanRecordRule {
    override fun check(item: AdaptiveLanePlanRecord): MutableList<String> {
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

data class AdaptiveLanePlanRecordGraceDaysRangeRule(var min: Int, var max: Int) : AdaptiveLanePlanRecordRule {
    override fun check(item: AdaptiveLanePlanRecord): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lane plan record. */
data class AdaptiveLanePlanRecordValidator(var rules: MutableList<AdaptiveLanePlanRecordRule>) {
    fun validate(item: AdaptiveLanePlanRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveLanePlanRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLanePlanRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLanePlanRecordValidator(): AdaptiveLanePlanRecordValidator {
    return AdaptiveLanePlanRecordValidator(
        mutableListOf<AdaptiveLanePlanRecordRule>(
            AdaptiveLanePlanRecordEndsAtRangeRule(64, 64),
            AdaptiveLanePlanRecordRequiredExternalRefRule("adaptive lane plan record needs an entry"),
            AdaptiveLanePlanRecordCodePrefixRule("ACM"),
            AdaptiveLanePlanRecordRequiredNameRule("adaptive lane plan record needs a value"),
            AdaptiveLanePlanRecordMarginRangeRule(5.1, 918.1),
            AdaptiveLanePlanRecordGraceDaysRangeRule(32, 1000),
        ),
    )
}
