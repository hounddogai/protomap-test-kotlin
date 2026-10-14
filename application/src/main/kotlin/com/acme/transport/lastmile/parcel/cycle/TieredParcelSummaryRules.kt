package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.text.normalize

/** A rule a tiered parcel summary must pass. Check returns its problems, or none. */
interface TieredParcelSummaryRule {
    fun check(item: TieredParcelSummary): MutableList<String>
}

data class TieredParcelSummaryRouteCodeLengthRule(var maxLength: Int) : TieredParcelSummaryRule {
    override fun check(item: TieredParcelSummary): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelSummaryRequiredCodeRule(var message: String) : TieredParcelSummaryRule {
    override fun check(item: TieredParcelSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelSummaryRequiredNameRule(var message: String) : TieredParcelSummaryRule {
    override fun check(item: TieredParcelSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelSummaryAllowedRouteCodeRule(var allowed: MutableList<String>) : TieredParcelSummaryRule {
    override fun check(item: TieredParcelSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelSummaryCodeLengthRule(var maxLength: Int) : TieredParcelSummaryRule {
    override fun check(item: TieredParcelSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered parcel summary. */
data class TieredParcelSummaryValidator(var rules: MutableList<TieredParcelSummaryRule>) {
    fun validate(item: TieredParcelSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredParcelSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredParcelSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredParcelSummaryValidator(): TieredParcelSummaryValidator {
    return TieredParcelSummaryValidator(
        mutableListOf<TieredParcelSummaryRule>(
            TieredParcelSummaryRouteCodeLengthRule(120),
            TieredParcelSummaryRequiredCodeRule("tiered parcel summary needs a value"),
            TieredParcelSummaryRequiredNameRule("tiered parcel summary needs a value"),
            TieredParcelSummaryAllowedRouteCodeRule(mutableListOf<String>("regional", "seasonal")),
            TieredParcelSummaryCodeLengthRule(86400),
        ),
    )
}
