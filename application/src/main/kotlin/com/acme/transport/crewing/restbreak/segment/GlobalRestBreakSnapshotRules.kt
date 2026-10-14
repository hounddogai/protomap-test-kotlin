package com.acme.transport.crewing.restbreak.segment

/** A rule a global rest break snapshot must pass. Check returns its problems, or none. */
interface GlobalRestBreakSnapshotRule {
    fun check(item: GlobalRestBreakSnapshot): MutableList<String>
}

data class GlobalRestBreakSnapshotTitleLengthRule(var maxLength: Int) : GlobalRestBreakSnapshotRule {
    override fun check(item: GlobalRestBreakSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakSnapshotCodePrefixRule(var prefix: String) : GlobalRestBreakSnapshotRule {
    override fun check(item: GlobalRestBreakSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakSnapshotRequiredCategoryRule(var message: String) : GlobalRestBreakSnapshotRule {
    override fun check(item: GlobalRestBreakSnapshot): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakSnapshotAttemptsRangeRule(var min: Int, var max: Int) : GlobalRestBreakSnapshotRule {
    override fun check(item: GlobalRestBreakSnapshot): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global rest break snapshot. */
data class GlobalRestBreakSnapshotValidator(var rules: MutableList<GlobalRestBreakSnapshotRule>) {
    fun validate(item: GlobalRestBreakSnapshot): MutableList<String> {
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

    fun requireValid(item: GlobalRestBreakSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRestBreakSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRestBreakSnapshotValidator(): GlobalRestBreakSnapshotValidator {
    return GlobalRestBreakSnapshotValidator(
        mutableListOf<GlobalRestBreakSnapshotRule>(
            GlobalRestBreakSnapshotTitleLengthRule(255),
            GlobalRestBreakSnapshotCodePrefixRule("REF"),
            GlobalRestBreakSnapshotRequiredCategoryRule("global rest break snapshot needs this field"),
            GlobalRestBreakSnapshotAttemptsRangeRule(64, 120),
        ),
    )
}
