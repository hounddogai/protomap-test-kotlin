package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.text.normalize

/** A rule a batched fuel burn override must pass. Check returns its problems, or none. */
interface BatchedFuelBurnOverrideRule {
    fun check(item: BatchedFuelBurnOverride): MutableList<String>
}

data class BatchedFuelBurnOverrideArchivedGuardRule(var limit: Int) : BatchedFuelBurnOverrideRule {
    override fun check(item: BatchedFuelBurnOverride): MutableList<String> {
        if (item.archived && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnOverrideAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : BatchedFuelBurnOverrideRule {
    override fun check(item: BatchedFuelBurnOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnOverrideRequiredNameRule(var message: String) : BatchedFuelBurnOverrideRule {
    override fun check(item: BatchedFuelBurnOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched fuel burn override. */
data class BatchedFuelBurnOverrideValidator(var rules: MutableList<BatchedFuelBurnOverrideRule>) {
    fun validate(item: BatchedFuelBurnOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedFuelBurnOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedFuelBurnOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedFuelBurnOverrideValidator(): BatchedFuelBurnOverrideValidator {
    return BatchedFuelBurnOverrideValidator(
        mutableListOf<BatchedFuelBurnOverrideRule>(
            BatchedFuelBurnOverrideArchivedGuardRule(1000),
            BatchedFuelBurnOverrideAllowedTimezoneRule(mutableListOf<String>("legacy", "adaptive", "capped")),
            BatchedFuelBurnOverrideRequiredNameRule("batched fuel burn override needs this field"),
        ),
    )
}
