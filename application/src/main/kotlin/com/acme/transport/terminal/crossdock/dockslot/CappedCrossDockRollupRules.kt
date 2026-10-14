package com.acme.transport.terminal.crossdock.dockslot

/** A rule a capped cross dock rollup must pass. Check returns its problems, or none. */
interface CappedCrossDockRollupRule {
    fun check(item: CappedCrossDockRollup): MutableList<String>
}

data class CappedCrossDockRollupReviewerLengthRule(var maxLength: Int) : CappedCrossDockRollupRule {
    override fun check(item: CappedCrossDockRollup): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockRollupCodePrefixRule(var prefix: String) : CappedCrossDockRollupRule {
    override fun check(item: CappedCrossDockRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockRollupArchivedGuardRule(var limit: Double) : CappedCrossDockRollupRule {
    override fun check(item: CappedCrossDockRollup): MutableList<String> {
        if (item.archived && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockRollupNameLengthRule(var maxLength: Int) : CappedCrossDockRollupRule {
    override fun check(item: CappedCrossDockRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockRollupCeilingRangeRule(var min: Double, var max: Double) : CappedCrossDockRollupRule {
    override fun check(item: CappedCrossDockRollup): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped cross dock rollup. */
data class CappedCrossDockRollupValidator(var rules: MutableList<CappedCrossDockRollupRule>) {
    fun validate(item: CappedCrossDockRollup): MutableList<String> {
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

    fun requireValid(item: CappedCrossDockRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCrossDockRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCrossDockRollupValidator(): CappedCrossDockRollupValidator {
    return CappedCrossDockRollupValidator(
        mutableListOf<CappedCrossDockRollupRule>(
            CappedCrossDockRollupReviewerLengthRule(80),
            CappedCrossDockRollupCodePrefixRule("X"),
            CappedCrossDockRollupArchivedGuardRule(8.4),
            CappedCrossDockRollupNameLengthRule(3600),
            CappedCrossDockRollupCeilingRangeRule(6.5, 2874.5),
        ),
    )
}
