package com.acme.transport.crewing.restbreak.breakvariance

/** A rule a tiered rest break rollup must pass. Check returns its problems, or none. */
interface TieredRestBreakRollupRule {
    fun check(item: TieredRestBreakRollup): MutableList<String>
}

data class TieredRestBreakRollupRequiredExternalRefRule(var message: String) : TieredRestBreakRollupRule {
    override fun check(item: TieredRestBreakRollup): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakRollupRequiredNameRule(var message: String) : TieredRestBreakRollupRule {
    override fun check(item: TieredRestBreakRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakRollupVisibleGuardRule(var limit: Int) : TieredRestBreakRollupRule {
    override fun check(item: TieredRestBreakRollup): MutableList<String> {
        if (item.visible && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered rest break rollup. */
data class TieredRestBreakRollupValidator(var rules: MutableList<TieredRestBreakRollupRule>) {
    fun validate(item: TieredRestBreakRollup): MutableList<String> {
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

    fun requireValid(item: TieredRestBreakRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRestBreakRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRestBreakRollupValidator(): TieredRestBreakRollupValidator {
    return TieredRestBreakRollupValidator(
        mutableListOf<TieredRestBreakRollupRule>(
            TieredRestBreakRollupRequiredExternalRefRule("tiered rest break rollup needs an entry"),
            TieredRestBreakRollupRequiredNameRule("tiered rest break rollup needs a value"),
            TieredRestBreakRollupVisibleGuardRule(64),
        ),
    )
}
