package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

/** A rule a primary route offset ledger must pass. Check returns its problems, or none. */
interface PrimaryRouteOffsetLedgerRule {
    fun check(item: PrimaryRouteOffsetLedger): MutableList<String>
}

data class PrimaryRouteOffsetLedgerCodePrefixRule(var prefix: String) : PrimaryRouteOffsetLedgerRule {
    override fun check(item: PrimaryRouteOffsetLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRouteOffsetLedgerRequiredLabelRule(var message: String) : PrimaryRouteOffsetLedgerRule {
    override fun check(item: PrimaryRouteOffsetLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRouteOffsetLedgerRequiredCodeRule(var message: String) : PrimaryRouteOffsetLedgerRule {
    override fun check(item: PrimaryRouteOffsetLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRouteOffsetLedgerCodeLengthRule(var maxLength: Int) : PrimaryRouteOffsetLedgerRule {
    override fun check(item: PrimaryRouteOffsetLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRouteOffsetLedgerLaneLengthRule(var maxLength: Int) : PrimaryRouteOffsetLedgerRule {
    override fun check(item: PrimaryRouteOffsetLedger): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRouteOffsetLedgerThresholdRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryRouteOffsetLedgerRule {
    override fun check(item: PrimaryRouteOffsetLedger): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary route offset ledger. */
data class PrimaryRouteOffsetLedgerValidator(var rules: MutableList<PrimaryRouteOffsetLedgerRule>) {
    fun validate(item: PrimaryRouteOffsetLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRouteOffsetLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRouteOffsetLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRouteOffsetLedgerValidator(): PrimaryRouteOffsetLedgerValidator {
    return PrimaryRouteOffsetLedgerValidator(
        mutableListOf<PrimaryRouteOffsetLedgerRule>(
            PrimaryRouteOffsetLedgerCodePrefixRule("ACM"),
            PrimaryRouteOffsetLedgerRequiredLabelRule("primary route offset ledger needs an entry"),
            PrimaryRouteOffsetLedgerRequiredCodeRule("primary route offset ledger needs a value"),
            PrimaryRouteOffsetLedgerCodeLengthRule(86400),
            PrimaryRouteOffsetLedgerLaneLengthRule(3600),
            PrimaryRouteOffsetLedgerThresholdRangeRule(0.3, 1683.8),
        ),
    )
}
