package com.acme.transport.customs.tariff.tariffdraft

/** A rule a batched tariff draft must pass. Check returns its problems, or none. */
interface BatchedTariffDraftRule {
    fun check(item: BatchedTariffDraft): MutableList<String>
}

data class BatchedTariffDraftRegionLengthRule(var maxLength: Int) : BatchedTariffDraftRule {
    override fun check(item: BatchedTariffDraft): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffDraftRequiredRegionRule(var message: String) : BatchedTariffDraftRule {
    override fun check(item: BatchedTariffDraft): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffDraftLabelLengthRule(var maxLength: Int) : BatchedTariffDraftRule {
    override fun check(item: BatchedTariffDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffDraftSequenceRangeRule(var min: Int, var max: Int) : BatchedTariffDraftRule {
    override fun check(item: BatchedTariffDraft): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class BatchedTariffDraftRequiredCodeRule(var message: String) : BatchedTariffDraftRule {
    override fun check(item: BatchedTariffDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tariff draft. */
data class BatchedTariffDraftValidator(var rules: MutableList<BatchedTariffDraftRule>) {
    fun validate(item: BatchedTariffDraft): MutableList<String> {
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

    fun requireValid(item: BatchedTariffDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTariffDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTariffDraftValidator(): BatchedTariffDraftValidator {
    return BatchedTariffDraftValidator(
        mutableListOf<BatchedTariffDraftRule>(
            BatchedTariffDraftRegionLengthRule(120),
            BatchedTariffDraftRequiredRegionRule("batched tariff draft needs a value"),
            BatchedTariffDraftLabelLengthRule(3600),
            BatchedTariffDraftSequenceRangeRule(16, 86400),
            BatchedTariffDraftRequiredCodeRule("batched tariff draft needs a value"),
        ),
    )
}
