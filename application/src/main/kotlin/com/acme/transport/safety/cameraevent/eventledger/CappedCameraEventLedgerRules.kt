package com.acme.transport.safety.cameraevent.eventledger

/** A rule a capped camera event ledger must pass. Check returns its problems, or none. */
interface CappedCameraEventLedgerRule {
    fun check(item: CappedCameraEventLedger): MutableList<String>
}

data class CappedCameraEventLedgerExpiresAtRangeRule(var min: Int, var max: Int) : CappedCameraEventLedgerRule {
    override fun check(item: CappedCameraEventLedger): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedCameraEventLedgerRequiredNameRule(var message: String) : CappedCameraEventLedgerRule {
    override fun check(item: CappedCameraEventLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCameraEventLedgerRatioRangeRule(
    var min: Double,
    var max: Double,
) : CappedCameraEventLedgerRule {
    override fun check(item: CappedCameraEventLedger): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped camera event ledger. */
data class CappedCameraEventLedgerValidator(var rules: MutableList<CappedCameraEventLedgerRule>) {
    fun validate(item: CappedCameraEventLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCameraEventLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCameraEventLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCameraEventLedgerValidator(): CappedCameraEventLedgerValidator {
    return CappedCameraEventLedgerValidator(
        mutableListOf<CappedCameraEventLedgerRule>(
            CappedCameraEventLedgerExpiresAtRangeRule(32, 3600),
            CappedCameraEventLedgerRequiredNameRule("capped camera event ledger needs this field"),
            CappedCameraEventLedgerRatioRangeRule(5.3, 2134.7),
        ),
    )
}
