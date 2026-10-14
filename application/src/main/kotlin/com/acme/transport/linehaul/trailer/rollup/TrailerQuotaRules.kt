package com.acme.transport.linehaul.trailer.rollup

/** A rule a trailer quota must pass. Check returns its problems, or none. */
interface TrailerQuotaRule {
    fun check(item: TrailerQuota): MutableList<String>
}

data class TrailerQuotaLimitRangeRule(var min: Int, var max: Int) : TrailerQuotaRule {
    override fun check(item: TrailerQuota): MutableList<String> {
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

data class TrailerQuotaRequiredTitleRule(var message: String) : TrailerQuotaRule {
    override fun check(item: TrailerQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerQuotaWeightRangeRule(var min: Double, var max: Double) : TrailerQuotaRule {
    override fun check(item: TrailerQuota): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerQuotaPriorityRangeRule(var min: Int, var max: Int) : TrailerQuotaRule {
    override fun check(item: TrailerQuota): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a trailer quota. */
data class TrailerQuotaValidator(var rules: MutableList<TrailerQuotaRule>) {
    fun validate(item: TrailerQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerQuotaValidator(): TrailerQuotaValidator {
    return TrailerQuotaValidator(
        mutableListOf<TrailerQuotaRule>(
            TrailerQuotaLimitRangeRule(1000, 86400),
            TrailerQuotaRequiredTitleRule("trailer quota needs this field"),
            TrailerQuotaWeightRangeRule(6.5, 510.7),
            TrailerQuotaPriorityRangeRule(64, 120),
        ),
    )
}
