package com.acme.transport.network.transittime.forecast

/** A rule a tiered transit time audit must pass. Check returns its problems, or none. */
interface TieredTransitTimeAuditRule {
    fun check(item: TieredTransitTimeAudit): MutableList<String>
}

data class TieredTransitTimeAuditCodePrefixRule(var prefix: String) : TieredTransitTimeAuditRule {
    override fun check(item: TieredTransitTimeAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeAuditRequiredSegmentRule(var message: String) : TieredTransitTimeAuditRule {
    override fun check(item: TieredTransitTimeAudit): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeAuditRequiredUnitRule(var message: String) : TieredTransitTimeAuditRule {
    override fun check(item: TieredTransitTimeAudit): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered transit time audit. */
data class TieredTransitTimeAuditValidator(var rules: MutableList<TieredTransitTimeAuditRule>) {
    fun validate(item: TieredTransitTimeAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredTransitTimeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTransitTimeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTransitTimeAuditValidator(): TieredTransitTimeAuditValidator {
    return TieredTransitTimeAuditValidator(
        mutableListOf<TieredTransitTimeAuditRule>(
            TieredTransitTimeAuditCodePrefixRule("REF"),
            TieredTransitTimeAuditRequiredSegmentRule("tiered transit time audit needs a value"),
            TieredTransitTimeAuditRequiredUnitRule("tiered transit time audit needs an entry"),
        ),
    )
}
