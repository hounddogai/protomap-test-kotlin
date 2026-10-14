package com.acme.transport.fleetops.utilization.utilizationsummary

/** A rule a seasonal utilization target must pass. Check returns its problems, or none. */
interface SeasonalUtilizationTargetRule {
    fun check(item: SeasonalUtilizationTarget): MutableList<String>
}

data class SeasonalUtilizationTargetActiveGuardRule(var limit: Int) : SeasonalUtilizationTargetRule {
    override fun check(item: SeasonalUtilizationTarget): MutableList<String> {
        if (item.active && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationTargetCodePrefixRule(var prefix: String) : SeasonalUtilizationTargetRule {
    override fun check(item: SeasonalUtilizationTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationTargetRequiredLocaleRule(var message: String) : SeasonalUtilizationTargetRule {
    override fun check(item: SeasonalUtilizationTarget): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationTargetRequiredDepotRule(var message: String) : SeasonalUtilizationTargetRule {
    override fun check(item: SeasonalUtilizationTarget): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationTargetRequiredCodeRule(var message: String) : SeasonalUtilizationTargetRule {
    override fun check(item: SeasonalUtilizationTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationTargetDepotLengthRule(var maxLength: Int) : SeasonalUtilizationTargetRule {
    override fun check(item: SeasonalUtilizationTarget): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal utilization target. */
data class SeasonalUtilizationTargetValidator(var rules: MutableList<SeasonalUtilizationTargetRule>) {
    fun validate(item: SeasonalUtilizationTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalUtilizationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalUtilizationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalUtilizationTargetValidator(): SeasonalUtilizationTargetValidator {
    return SeasonalUtilizationTargetValidator(
        mutableListOf<SeasonalUtilizationTargetRule>(
            SeasonalUtilizationTargetActiveGuardRule(64),
            SeasonalUtilizationTargetCodePrefixRule("REF"),
            SeasonalUtilizationTargetRequiredLocaleRule("seasonal utilization target needs a value"),
            SeasonalUtilizationTargetRequiredDepotRule("seasonal utilization target needs an entry"),
            SeasonalUtilizationTargetRequiredCodeRule("seasonal utilization target needs a value"),
            SeasonalUtilizationTargetDepotLengthRule(16),
        ),
    )
}
