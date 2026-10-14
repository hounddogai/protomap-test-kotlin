package com.acme.transport.linehaul.backhaul.reservation

/** A rule a backhaul quota must pass. Check returns its problems, or none. */
interface BackhaulQuotaRule {
    fun check(item: BackhaulQuota): MutableList<String>
}

data class BackhaulQuotaRequiredDescriptionRule(var message: String) : BackhaulQuotaRule {
    override fun check(item: BackhaulQuota): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulQuotaRequiredNameRule(var message: String) : BackhaulQuotaRule {
    override fun check(item: BackhaulQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulQuotaWindowDaysRangeRule(var min: Int, var max: Int) : BackhaulQuotaRule {
    override fun check(item: BackhaulQuota): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulQuotaCategoryLengthRule(var maxLength: Int) : BackhaulQuotaRule {
    override fun check(item: BackhaulQuota): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul quota. */
data class BackhaulQuotaValidator(var rules: MutableList<BackhaulQuotaRule>) {
    fun validate(item: BackhaulQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulQuotaValidator(): BackhaulQuotaValidator {
    return BackhaulQuotaValidator(
        mutableListOf<BackhaulQuotaRule>(
            BackhaulQuotaRequiredDescriptionRule("backhaul quota needs an entry"),
            BackhaulQuotaRequiredNameRule("backhaul quota needs an entry"),
            BackhaulQuotaWindowDaysRangeRule(64, 255),
            BackhaulQuotaCategoryLengthRule(32),
        ),
    )
}
