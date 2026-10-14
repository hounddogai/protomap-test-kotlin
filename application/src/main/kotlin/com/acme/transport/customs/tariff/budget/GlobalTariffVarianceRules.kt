package com.acme.transport.customs.tariff.budget

/** A rule a global tariff variance must pass. Check returns its problems, or none. */
interface GlobalTariffVarianceRule {
    fun check(item: GlobalTariffVariance): MutableList<String>
}

data class GlobalTariffVarianceTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : GlobalTariffVarianceRule {
    override fun check(item: GlobalTariffVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class GlobalTariffVarianceRequiredTimezoneRule(var message: String) : GlobalTariffVarianceRule {
    override fun check(item: GlobalTariffVariance): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTariffVarianceLabelLengthRule(var maxLength: Int) : GlobalTariffVarianceRule {
    override fun check(item: GlobalTariffVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tariff variance. */
data class GlobalTariffVarianceValidator(var rules: MutableList<GlobalTariffVarianceRule>) {
    fun validate(item: GlobalTariffVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTariffVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTariffVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTariffVarianceValidator(): GlobalTariffVarianceValidator {
    return GlobalTariffVarianceValidator(
        mutableListOf<GlobalTariffVarianceRule>(
            GlobalTariffVarianceTagsLimitRule(64, mutableListOf<String>("adaptive", "capped")),
            GlobalTariffVarianceRequiredTimezoneRule("global tariff variance needs this field"),
            GlobalTariffVarianceLabelLengthRule(80),
        ),
    )
}
