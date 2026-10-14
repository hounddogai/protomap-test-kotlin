package com.acme.transport.freightpay.dispute.ledger

/** A rule a rolling dispute ledger must pass. Check returns its problems, or none. */
interface RollingDisputeLedgerRule {
    fun check(item: RollingDisputeLedger): MutableList<String>
}

data class RollingDisputeLedgerEndsAtRangeRule(var min: Int, var max: Int) : RollingDisputeLedgerRule {
    override fun check(item: RollingDisputeLedger): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputeLedgerCapacityRangeRule(var min: Int, var max: Int) : RollingDisputeLedgerRule {
    override fun check(item: RollingDisputeLedger): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputeLedgerRequiredNameRule(var message: String) : RollingDisputeLedgerRule {
    override fun check(item: RollingDisputeLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputeLedgerVisibleGuardRule(var limit: Int) : RollingDisputeLedgerRule {
    override fun check(item: RollingDisputeLedger): MutableList<String> {
        if (item.visible && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputeLedgerCodePrefixRule(var prefix: String) : RollingDisputeLedgerRule {
    override fun check(item: RollingDisputeLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dispute ledger. */
data class RollingDisputeLedgerValidator(var rules: MutableList<RollingDisputeLedgerRule>) {
    fun validate(item: RollingDisputeLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDisputeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDisputeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDisputeLedgerValidator(): RollingDisputeLedgerValidator {
    return RollingDisputeLedgerValidator(
        mutableListOf<RollingDisputeLedgerRule>(
            RollingDisputeLedgerEndsAtRangeRule(120, 255),
            RollingDisputeLedgerCapacityRangeRule(64, 1000),
            RollingDisputeLedgerRequiredNameRule("rolling dispute ledger needs this field"),
            RollingDisputeLedgerVisibleGuardRule(1000),
            RollingDisputeLedgerCodePrefixRule("Q"),
        ),
    )
}
