package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.kit.text.normalize

/** A rule a legacy rest break snapshot must pass. Check returns its problems, or none. */
interface LegacyRestBreakSnapshotRule {
    fun check(item: LegacyRestBreakSnapshot): MutableList<String>
}

data class LegacyRestBreakSnapshotRequiredExternalRefRule(var message: String) : LegacyRestBreakSnapshotRule {
    override fun check(item: LegacyRestBreakSnapshot): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakSnapshotRequiredCodeRule(var message: String) : LegacyRestBreakSnapshotRule {
    override fun check(item: LegacyRestBreakSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakSnapshotMinimumRangeRule(
    var min: Double,
    var max: Double,
) : LegacyRestBreakSnapshotRule {
    override fun check(item: LegacyRestBreakSnapshot): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakSnapshotAllowedPhoneRule(
    var allowed: MutableList<String>,
) : LegacyRestBreakSnapshotRule {
    override fun check(item: LegacyRestBreakSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakSnapshotRetriesRangeRule(var min: Int, var max: Int) : LegacyRestBreakSnapshotRule {
    override fun check(item: LegacyRestBreakSnapshot): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy rest break snapshot. */
data class LegacyRestBreakSnapshotValidator(var rules: MutableList<LegacyRestBreakSnapshotRule>) {
    fun validate(item: LegacyRestBreakSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyRestBreakSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRestBreakSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRestBreakSnapshotValidator(): LegacyRestBreakSnapshotValidator {
    return LegacyRestBreakSnapshotValidator(
        mutableListOf<LegacyRestBreakSnapshotRule>(
            LegacyRestBreakSnapshotRequiredExternalRefRule("legacy rest break snapshot needs an entry"),
            LegacyRestBreakSnapshotRequiredCodeRule("legacy rest break snapshot needs this field"),
            LegacyRestBreakSnapshotMinimumRangeRule(8.5, 4088.5),
            LegacyRestBreakSnapshotAllowedPhoneRule(mutableListOf<String>("scheduled")),
            LegacyRestBreakSnapshotRetriesRangeRule(32, 86400),
        ),
    )
}
