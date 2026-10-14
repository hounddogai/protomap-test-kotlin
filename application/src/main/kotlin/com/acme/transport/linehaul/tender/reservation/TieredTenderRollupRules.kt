package com.acme.transport.linehaul.tender.reservation

import com.acme.transport.kit.text.normalize

/** A rule a tiered tender rollup must pass. Check returns its problems, or none. */
interface TieredTenderRollupRule {
    fun check(item: TieredTenderRollup): MutableList<String>
}

data class TieredTenderRollupRequiredCodeRule(var message: String) : TieredTenderRollupRule {
    override fun check(item: TieredTenderRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTenderRollupBatchSizeRangeRule(var min: Int, var max: Int) : TieredTenderRollupRule {
    override fun check(item: TieredTenderRollup): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class TieredTenderRollupAllowedNameRule(var allowed: MutableList<String>) : TieredTenderRollupRule {
    override fun check(item: TieredTenderRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tender rollup. */
data class TieredTenderRollupValidator(var rules: MutableList<TieredTenderRollupRule>) {
    fun validate(item: TieredTenderRollup): MutableList<String> {
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

    fun requireValid(item: TieredTenderRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTenderRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTenderRollupValidator(): TieredTenderRollupValidator {
    return TieredTenderRollupValidator(
        mutableListOf<TieredTenderRollupRule>(
            TieredTenderRollupRequiredCodeRule("tiered tender rollup needs an entry"),
            TieredTenderRollupBatchSizeRangeRule(16, 255),
            TieredTenderRollupAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
