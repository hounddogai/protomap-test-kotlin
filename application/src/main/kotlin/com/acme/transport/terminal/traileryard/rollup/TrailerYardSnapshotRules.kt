package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard snapshot must pass. Check returns its problems, or none. */
interface TrailerYardSnapshotRule {
    fun check(item: TrailerYardSnapshot): MutableList<String>
}

data class TrailerYardSnapshotAllowedNameRule(var allowed: MutableList<String>) : TrailerYardSnapshotRule {
    override fun check(item: TrailerYardSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardSnapshotRequiredCodeRule(var message: String) : TrailerYardSnapshotRule {
    override fun check(item: TrailerYardSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardSnapshotNameLengthRule(var maxLength: Int) : TrailerYardSnapshotRule {
    override fun check(item: TrailerYardSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardSnapshotCategoryLengthRule(var maxLength: Int) : TrailerYardSnapshotRule {
    override fun check(item: TrailerYardSnapshot): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardSnapshotCostRangeRule(var min: Double, var max: Double) : TrailerYardSnapshotRule {
    override fun check(item: TrailerYardSnapshot): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard snapshot. */
data class TrailerYardSnapshotValidator(var rules: MutableList<TrailerYardSnapshotRule>) {
    fun validate(item: TrailerYardSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardSnapshotValidator(): TrailerYardSnapshotValidator {
    return TrailerYardSnapshotValidator(
        mutableListOf<TrailerYardSnapshotRule>(
            TrailerYardSnapshotAllowedNameRule(mutableListOf<String>("primary", "seasonal", "default")),
            TrailerYardSnapshotRequiredCodeRule("trailer yard snapshot needs an entry"),
            TrailerYardSnapshotNameLengthRule(3600),
            TrailerYardSnapshotCategoryLengthRule(16),
            TrailerYardSnapshotCostRangeRule(3.0, 4330.7),
        ),
    )
}
