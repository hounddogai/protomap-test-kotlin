package com.acme.transport.network.node.batch

/** A rule a legacy node batch must pass. Check returns its problems, or none. */
interface LegacyNodeBatchRule {
    fun check(item: LegacyNodeBatch): MutableList<String>
}

data class LegacyNodeBatchArchivedGuardRule(var limit: Double) : LegacyNodeBatchRule {
    override fun check(item: LegacyNodeBatch): MutableList<String> {
        if (item.archived && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeBatchCodePrefixRule(var prefix: String) : LegacyNodeBatchRule {
    override fun check(item: LegacyNodeBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeBatchRequiredTitleRule(var message: String) : LegacyNodeBatchRule {
    override fun check(item: LegacyNodeBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy node batch. */
data class LegacyNodeBatchValidator(var rules: MutableList<LegacyNodeBatchRule>) {
    fun validate(item: LegacyNodeBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: LegacyNodeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyNodeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyNodeBatchValidator(): LegacyNodeBatchValidator {
    return LegacyNodeBatchValidator(
        mutableListOf<LegacyNodeBatchRule>(
            LegacyNodeBatchArchivedGuardRule(1.4),
            LegacyNodeBatchCodePrefixRule("X"),
            LegacyNodeBatchRequiredTitleRule("legacy node batch needs this field"),
        ),
    )
}
