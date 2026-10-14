package com.acme.transport.network.hub.summary

import com.acme.transport.kit.text.normalize

/** A rule a batched hub record must pass. Check returns its problems, or none. */
interface BatchedHubRecordRule {
    fun check(item: BatchedHubRecord): MutableList<String>
}

data class BatchedHubRecordCodeLengthRule(var maxLength: Int) : BatchedHubRecordRule {
    override fun check(item: BatchedHubRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubRecordRequiredVehicleIdRule(var message: String) : BatchedHubRecordRule {
    override fun check(item: BatchedHubRecord): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubRecordRequiredCodeRule(var message: String) : BatchedHubRecordRule {
    override fun check(item: BatchedHubRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubRecordAllowedTitleRule(var allowed: MutableList<String>) : BatchedHubRecordRule {
    override fun check(item: BatchedHubRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubRecordRequiredCategoryRule(var message: String) : BatchedHubRecordRule {
    override fun check(item: BatchedHubRecord): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched hub record. */
data class BatchedHubRecordValidator(var rules: MutableList<BatchedHubRecordRule>) {
    fun validate(item: BatchedHubRecord): MutableList<String> {
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

    fun requireValid(item: BatchedHubRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedHubRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedHubRecordValidator(): BatchedHubRecordValidator {
    return BatchedHubRecordValidator(
        mutableListOf<BatchedHubRecordRule>(
            BatchedHubRecordCodeLengthRule(1000),
            BatchedHubRecordRequiredVehicleIdRule("batched hub record needs a value"),
            BatchedHubRecordRequiredCodeRule("batched hub record needs a value"),
            BatchedHubRecordAllowedTitleRule(mutableListOf<String>()),
            BatchedHubRecordRequiredCategoryRule("batched hub record needs an entry"),
        ),
    )
}
