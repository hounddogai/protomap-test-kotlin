package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a batched transit time entry must pass. Check returns its problems, or none. */
interface BatchedTransitTimeEntryRule {
    fun check(item: BatchedTransitTimeEntry): MutableList<String>
}

data class BatchedTransitTimeEntryRequiredTitleRule(var message: String) : BatchedTransitTimeEntryRule {
    override fun check(item: BatchedTransitTimeEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTransitTimeEntryAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : BatchedTransitTimeEntryRule {
    override fun check(item: BatchedTransitTimeEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTransitTimeEntryRequiredCodeRule(var message: String) : BatchedTransitTimeEntryRule {
    override fun check(item: BatchedTransitTimeEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTransitTimeEntryDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : BatchedTransitTimeEntryRule {
    override fun check(item: BatchedTransitTimeEntry): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched transit time entry. */
data class BatchedTransitTimeEntryValidator(var rules: MutableList<BatchedTransitTimeEntryRule>) {
    fun validate(item: BatchedTransitTimeEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTransitTimeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTransitTimeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTransitTimeEntryValidator(): BatchedTransitTimeEntryValidator {
    return BatchedTransitTimeEntryValidator(
        mutableListOf<BatchedTransitTimeEntryRule>(
            BatchedTransitTimeEntryRequiredTitleRule("batched transit time entry needs an entry"),
            BatchedTransitTimeEntryAllowedOwnerIdRule(mutableListOf<String>("global", "adaptive")),
            BatchedTransitTimeEntryRequiredCodeRule("batched transit time entry needs an entry"),
            BatchedTransitTimeEntryDistanceKmRangeRule(9.1, 3259.1),
        ),
    )
}
