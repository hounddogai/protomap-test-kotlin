package com.acme.transport.network.node.manifest

/** A rule a tiered node estimate must pass. Check returns its problems, or none. */
interface TieredNodeEstimateRule {
    fun check(item: TieredNodeEstimate): MutableList<String>
}

data class TieredNodeEstimateCodePrefixRule(var prefix: String) : TieredNodeEstimateRule {
    override fun check(item: TieredNodeEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeEstimateRequiredShiftCodeRule(var message: String) : TieredNodeEstimateRule {
    override fun check(item: TieredNodeEstimate): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeEstimateRequiredCodeRule(var message: String) : TieredNodeEstimateRule {
    override fun check(item: TieredNodeEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeEstimateRequiredTitleRule(var message: String) : TieredNodeEstimateRule {
    override fun check(item: TieredNodeEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered node estimate. */
data class TieredNodeEstimateValidator(var rules: MutableList<TieredNodeEstimateRule>) {
    fun validate(item: TieredNodeEstimate): MutableList<String> {
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

    fun requireValid(item: TieredNodeEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredNodeEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredNodeEstimateValidator(): TieredNodeEstimateValidator {
    return TieredNodeEstimateValidator(
        mutableListOf<TieredNodeEstimateRule>(
            TieredNodeEstimateCodePrefixRule("REF"),
            TieredNodeEstimateRequiredShiftCodeRule("tiered node estimate needs this field"),
            TieredNodeEstimateRequiredCodeRule("tiered node estimate needs this field"),
            TieredNodeEstimateRequiredTitleRule("tiered node estimate needs this field"),
        ),
    )
}
