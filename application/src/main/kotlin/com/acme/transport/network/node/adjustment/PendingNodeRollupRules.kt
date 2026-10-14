package com.acme.transport.network.node.adjustment

/** A rule a pending node rollup must pass. Check returns its problems, or none. */
interface PendingNodeRollupRule {
    fun check(item: PendingNodeRollup): MutableList<String>
}

data class PendingNodeRollupSequenceRangeRule(var min: Int, var max: Int) : PendingNodeRollupRule {
    override fun check(item: PendingNodeRollup): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class PendingNodeRollupRequiredCodeRule(var message: String) : PendingNodeRollupRule {
    override fun check(item: PendingNodeRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeRollupPinnedGuardRule(var limit: Int) : PendingNodeRollupRule {
    override fun check(item: PendingNodeRollup): MutableList<String> {
        if (item.pinned && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeRollupSourceLengthRule(var maxLength: Int) : PendingNodeRollupRule {
    override fun check(item: PendingNodeRollup): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeRollupBatchSizeRangeRule(var min: Int, var max: Int) : PendingNodeRollupRule {
    override fun check(item: PendingNodeRollup): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending node rollup. */
data class PendingNodeRollupValidator(var rules: MutableList<PendingNodeRollupRule>) {
    fun validate(item: PendingNodeRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingNodeRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNodeRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNodeRollupValidator(): PendingNodeRollupValidator {
    return PendingNodeRollupValidator(
        mutableListOf<PendingNodeRollupRule>(
            PendingNodeRollupSequenceRangeRule(120, 3600),
            PendingNodeRollupRequiredCodeRule("pending node rollup needs an entry"),
            PendingNodeRollupPinnedGuardRule(86400),
            PendingNodeRollupSourceLengthRule(86400),
            PendingNodeRollupBatchSizeRangeRule(32, 120),
        ),
    )
}
