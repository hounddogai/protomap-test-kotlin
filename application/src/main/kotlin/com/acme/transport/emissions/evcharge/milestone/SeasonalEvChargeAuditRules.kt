package com.acme.transport.emissions.evcharge.milestone

/** A rule a seasonal ev charge audit must pass. Check returns its problems, or none. */
interface SeasonalEvChargeAuditRule {
    fun check(item: SeasonalEvChargeAudit): MutableList<String>
}

data class SeasonalEvChargeAuditCodePrefixRule(var prefix: String) : SeasonalEvChargeAuditRule {
    override fun check(item: SeasonalEvChargeAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeAuditLabelLengthRule(var maxLength: Int) : SeasonalEvChargeAuditRule {
    override fun check(item: SeasonalEvChargeAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeAuditExternalRefLengthRule(var maxLength: Int) : SeasonalEvChargeAuditRule {
    override fun check(item: SeasonalEvChargeAudit): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal ev charge audit. */
data class SeasonalEvChargeAuditValidator(var rules: MutableList<SeasonalEvChargeAuditRule>) {
    fun validate(item: SeasonalEvChargeAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalEvChargeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEvChargeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEvChargeAuditValidator(): SeasonalEvChargeAuditValidator {
    return SeasonalEvChargeAuditValidator(
        mutableListOf<SeasonalEvChargeAuditRule>(
            SeasonalEvChargeAuditCodePrefixRule("ACM"),
            SeasonalEvChargeAuditLabelLengthRule(1000),
            SeasonalEvChargeAuditExternalRefLengthRule(1000),
        ),
    )
}
