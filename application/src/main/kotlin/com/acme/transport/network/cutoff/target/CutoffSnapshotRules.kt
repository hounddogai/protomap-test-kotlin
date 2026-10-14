package com.acme.transport.network.cutoff.target

/** A rule a cutoff snapshot must pass. Check returns its problems, or none. */
interface CutoffSnapshotRule {
    fun check(item: CutoffSnapshot): MutableList<String>
}

data class CutoffSnapshotTitleLengthRule(var maxLength: Int) : CutoffSnapshotRule {
    override fun check(item: CutoffSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffSnapshotCodePrefixRule(var prefix: String) : CutoffSnapshotRule {
    override fun check(item: CutoffSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffSnapshotUpdatedAtRangeRule(var min: Int, var max: Int) : CutoffSnapshotRule {
    override fun check(item: CutoffSnapshot): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a cutoff snapshot. */
data class CutoffSnapshotValidator(var rules: MutableList<CutoffSnapshotRule>) {
    fun validate(item: CutoffSnapshot): MutableList<String> {
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

    fun requireValid(item: CutoffSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffSnapshotValidator(): CutoffSnapshotValidator {
    return CutoffSnapshotValidator(
        mutableListOf<CutoffSnapshotRule>(
            CutoffSnapshotTitleLengthRule(86400),
            CutoffSnapshotCodePrefixRule("Q"),
            CutoffSnapshotUpdatedAtRangeRule(64, 1000),
        ),
    )
}
