package com.acme.transport.customs.hold.variance

/** A rule a seasonal hold audit must pass. Check returns its problems, or none. */
interface SeasonalHoldAuditRule {
    fun check(item: SeasonalHoldAudit): MutableList<String>
}

data class SeasonalHoldAuditCodePrefixRule(var prefix: String) : SeasonalHoldAuditRule {
    override fun check(item: SeasonalHoldAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHoldAuditRequiredOwnerIdRule(var message: String) : SeasonalHoldAuditRule {
    override fun check(item: SeasonalHoldAudit): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHoldAuditRequiredCodeRule(var message: String) : SeasonalHoldAuditRule {
    override fun check(item: SeasonalHoldAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHoldAuditLoadKgRangeRule(var min: Double, var max: Double) : SeasonalHoldAuditRule {
    override fun check(item: SeasonalHoldAudit): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class SeasonalHoldAuditDistanceKmRangeRule(var min: Double, var max: Double) : SeasonalHoldAuditRule {
    override fun check(item: SeasonalHoldAudit): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal hold audit. */
data class SeasonalHoldAuditValidator(var rules: MutableList<SeasonalHoldAuditRule>) {
    fun validate(item: SeasonalHoldAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalHoldAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalHoldAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalHoldAuditValidator(): SeasonalHoldAuditValidator {
    return SeasonalHoldAuditValidator(
        mutableListOf<SeasonalHoldAuditRule>(
            SeasonalHoldAuditCodePrefixRule("ACM"),
            SeasonalHoldAuditRequiredOwnerIdRule("seasonal hold audit needs this field"),
            SeasonalHoldAuditRequiredCodeRule("seasonal hold audit needs this field"),
            SeasonalHoldAuditLoadKgRangeRule(0.1, 2124.2),
            SeasonalHoldAuditDistanceKmRangeRule(1.0, 323.0),
        ),
    )
}
