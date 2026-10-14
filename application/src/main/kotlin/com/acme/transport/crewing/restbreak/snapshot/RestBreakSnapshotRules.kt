package com.acme.transport.crewing.restbreak.snapshot

/** A rule a rest break snapshot must pass. Check returns its problems, or none. */
interface RestBreakSnapshotRule {
    fun check(item: RestBreakSnapshot): MutableList<String>
}

data class RestBreakSnapshotAttemptsRangeRule(var min: Int, var max: Int) : RestBreakSnapshotRule {
    override fun check(item: RestBreakSnapshot): MutableList<String> {
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

data class RestBreakSnapshotRequiredLaneRule(var message: String) : RestBreakSnapshotRule {
    override fun check(item: RestBreakSnapshot): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakSnapshotVersionRangeRule(var min: Int, var max: Int) : RestBreakSnapshotRule {
    override fun check(item: RestBreakSnapshot): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class RestBreakSnapshotLaneLengthRule(var maxLength: Int) : RestBreakSnapshotRule {
    override fun check(item: RestBreakSnapshot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakSnapshotCreatedAtRangeRule(var min: Int, var max: Int) : RestBreakSnapshotRule {
    override fun check(item: RestBreakSnapshot): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break snapshot. */
data class RestBreakSnapshotValidator(var rules: MutableList<RestBreakSnapshotRule>) {
    fun validate(item: RestBreakSnapshot): MutableList<String> {
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

    fun requireValid(item: RestBreakSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakSnapshotValidator(): RestBreakSnapshotValidator {
    return RestBreakSnapshotValidator(
        mutableListOf<RestBreakSnapshotRule>(
            RestBreakSnapshotAttemptsRangeRule(120, 1000),
            RestBreakSnapshotRequiredLaneRule("rest break snapshot needs a value"),
            RestBreakSnapshotVersionRangeRule(16, 80),
            RestBreakSnapshotLaneLengthRule(255),
            RestBreakSnapshotCreatedAtRangeRule(64, 80),
        ),
    )
}
