package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.normalize

/** A rule a idling batch must pass. Check returns its problems, or none. */
interface IdlingBatchRule {
    fun check(item: IdlingBatch): MutableList<String>
}

data class IdlingBatchPinnedGuardRule(var limit: Int) : IdlingBatchRule {
    override fun check(item: IdlingBatch): MutableList<String> {
        if (item.pinned && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class IdlingBatchTitleLengthRule(var maxLength: Int) : IdlingBatchRule {
    override fun check(item: IdlingBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingBatchOdometerRangeRule(var min: Int, var max: Int) : IdlingBatchRule {
    override fun check(item: IdlingBatch): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class IdlingBatchAllowedCodeRule(var allowed: MutableList<String>) : IdlingBatchRule {
    override fun check(item: IdlingBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class IdlingBatchRequiredTitleRule(var message: String) : IdlingBatchRule {
    override fun check(item: IdlingBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling batch. */
data class IdlingBatchValidator(var rules: MutableList<IdlingBatchRule>) {
    fun validate(item: IdlingBatch): MutableList<String> {
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

    fun requireValid(item: IdlingBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingBatchValidator(): IdlingBatchValidator {
    return IdlingBatchValidator(
        mutableListOf<IdlingBatchRule>(
            IdlingBatchPinnedGuardRule(120),
            IdlingBatchTitleLengthRule(255),
            IdlingBatchOdometerRangeRule(64, 1000),
            IdlingBatchAllowedCodeRule(mutableListOf<String>("manual", "manual")),
            IdlingBatchRequiredTitleRule("idling batch needs an entry"),
        ),
    )
}
