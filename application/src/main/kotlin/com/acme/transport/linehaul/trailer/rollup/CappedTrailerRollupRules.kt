package com.acme.transport.linehaul.trailer.rollup

/** A rule a capped trailer rollup must pass. Check returns its problems, or none. */
interface CappedTrailerRollupRule {
    fun check(item: CappedTrailerRollup): MutableList<String>
}

data class CappedTrailerRollupPinnedGuardRule(var limit: Int) : CappedTrailerRollupRule {
    override fun check(item: CappedTrailerRollup): MutableList<String> {
        if (item.pinned && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerRollupDockLengthRule(var maxLength: Int) : CappedTrailerRollupRule {
    override fun check(item: CappedTrailerRollup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerRollupRequiredDescriptionRule(var message: String) : CappedTrailerRollupRule {
    override fun check(item: CappedTrailerRollup): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerRollupRateRangeRule(var min: Double, var max: Double) : CappedTrailerRollupRule {
    override fun check(item: CappedTrailerRollup): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped trailer rollup. */
data class CappedTrailerRollupValidator(var rules: MutableList<CappedTrailerRollupRule>) {
    fun validate(item: CappedTrailerRollup): MutableList<String> {
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

    fun requireValid(item: CappedTrailerRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerRollupValidator(): CappedTrailerRollupValidator {
    return CappedTrailerRollupValidator(
        mutableListOf<CappedTrailerRollupRule>(
            CappedTrailerRollupPinnedGuardRule(255),
            CappedTrailerRollupDockLengthRule(64),
            CappedTrailerRollupRequiredDescriptionRule("capped trailer rollup needs a value"),
            CappedTrailerRollupRateRangeRule(7.7, 4772.8),
        ),
    )
}
