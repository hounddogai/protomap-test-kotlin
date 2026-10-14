package com.acme.transport.network.zone.zonequota

/** A rule a regional zone quota must pass. Check returns its problems, or none. */
interface RegionalZoneQuotaRule {
    fun check(item: RegionalZoneQuota): MutableList<String>
}

data class RegionalZoneQuotaRevisionRangeRule(var min: Int, var max: Int) : RegionalZoneQuotaRule {
    override fun check(item: RegionalZoneQuota): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneQuotaRatioRangeRule(var min: Double, var max: Double) : RegionalZoneQuotaRule {
    override fun check(item: RegionalZoneQuota): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneQuotaRequiredCodeRule(var message: String) : RegionalZoneQuotaRule {
    override fun check(item: RegionalZoneQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneQuotaDurationMinutesRangeRule(var min: Int, var max: Int) : RegionalZoneQuotaRule {
    override fun check(item: RegionalZoneQuota): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneQuotaAttemptsRangeRule(var min: Int, var max: Int) : RegionalZoneQuotaRule {
    override fun check(item: RegionalZoneQuota): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional zone quota. */
data class RegionalZoneQuotaValidator(var rules: MutableList<RegionalZoneQuotaRule>) {
    fun validate(item: RegionalZoneQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalZoneQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalZoneQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalZoneQuotaValidator(): RegionalZoneQuotaValidator {
    return RegionalZoneQuotaValidator(
        mutableListOf<RegionalZoneQuotaRule>(
            RegionalZoneQuotaRevisionRangeRule(64, 64),
            RegionalZoneQuotaRatioRangeRule(6.4, 962.5),
            RegionalZoneQuotaRequiredCodeRule("regional zone quota needs this field"),
            RegionalZoneQuotaDurationMinutesRangeRule(32, 120),
            RegionalZoneQuotaAttemptsRangeRule(255, 255),
        ),
    )
}
