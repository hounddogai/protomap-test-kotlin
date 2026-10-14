package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.text.normalize

/** A rule a batched tariff rollup must pass. Check returns its problems, or none. */
interface BatchedTariffRollupRule {
    fun check(item: BatchedTariffRollup): MutableList<String>
}

data class BatchedTariffRollupRequiredRegionRule(var message: String) : BatchedTariffRollupRule {
    override fun check(item: BatchedTariffRollup): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffRollupAllowedNameRule(var allowed: MutableList<String>) : BatchedTariffRollupRule {
    override fun check(item: BatchedTariffRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffRollupRequiredCodeRule(var message: String) : BatchedTariffRollupRule {
    override fun check(item: BatchedTariffRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffRollupRequiredPhoneRule(var message: String) : BatchedTariffRollupRule {
    override fun check(item: BatchedTariffRollup): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffRollupVisibleGuardRule(var limit: Int) : BatchedTariffRollupRule {
    override fun check(item: BatchedTariffRollup): MutableList<String> {
        if (item.visible && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tariff rollup. */
data class BatchedTariffRollupValidator(var rules: MutableList<BatchedTariffRollupRule>) {
    fun validate(item: BatchedTariffRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTariffRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTariffRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTariffRollupValidator(): BatchedTariffRollupValidator {
    return BatchedTariffRollupValidator(
        mutableListOf<BatchedTariffRollupRule>(
            BatchedTariffRollupRequiredRegionRule("batched tariff rollup needs this field"),
            BatchedTariffRollupAllowedNameRule(mutableListOf<String>("global", "strict", "weighted")),
            BatchedTariffRollupRequiredCodeRule("batched tariff rollup needs this field"),
            BatchedTariffRollupRequiredPhoneRule("batched tariff rollup needs a value"),
            BatchedTariffRollupVisibleGuardRule(3600),
        ),
    )
}
