package com.acme.transport.linehaul.relay.weightedrelayrule

/** A rule a pending relay rollup must pass. Check returns its problems, or none. */
interface PendingRelayRollupRule {
    fun check(item: PendingRelayRollup): MutableList<String>
}

data class PendingRelayRollupPalletsRangeRule(var min: Int, var max: Int) : PendingRelayRollupRule {
    override fun check(item: PendingRelayRollup): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayRollupTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : PendingRelayRollupRule {
    override fun check(item: PendingRelayRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class PendingRelayRollupBatchSizeRangeRule(var min: Int, var max: Int) : PendingRelayRollupRule {
    override fun check(item: PendingRelayRollup): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending relay rollup. */
data class PendingRelayRollupValidator(var rules: MutableList<PendingRelayRollupRule>) {
    fun validate(item: PendingRelayRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRelayRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRelayRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRelayRollupValidator(): PendingRelayRollupValidator {
    return PendingRelayRollupValidator(
        mutableListOf<PendingRelayRollupRule>(
            PendingRelayRollupPalletsRangeRule(64, 120),
            PendingRelayRollupTagsLimitRule(1000, mutableListOf<String>("default")),
            PendingRelayRollupBatchSizeRangeRule(3600, 86400),
        ),
    )
}
