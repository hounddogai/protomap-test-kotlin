package com.acme.transport.lastmile.locker.batch

/** A rule a global locker batch must pass. Check returns its problems, or none. */
interface GlobalLockerBatchRule {
    fun check(item: GlobalLockerBatch): MutableList<String>
}

data class GlobalLockerBatchTrailerIdLengthRule(var maxLength: Int) : GlobalLockerBatchRule {
    override fun check(item: GlobalLockerBatch): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerBatchRequiredCodeRule(var message: String) : GlobalLockerBatchRule {
    override fun check(item: GlobalLockerBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerBatchLocaleLengthRule(var maxLength: Int) : GlobalLockerBatchRule {
    override fun check(item: GlobalLockerBatch): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerBatchRetriesRangeRule(var min: Int, var max: Int) : GlobalLockerBatchRule {
    override fun check(item: GlobalLockerBatch): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global locker batch. */
data class GlobalLockerBatchValidator(var rules: MutableList<GlobalLockerBatchRule>) {
    fun validate(item: GlobalLockerBatch): MutableList<String> {
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

    fun requireValid(item: GlobalLockerBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalLockerBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalLockerBatchValidator(): GlobalLockerBatchValidator {
    return GlobalLockerBatchValidator(
        mutableListOf<GlobalLockerBatchRule>(
            GlobalLockerBatchTrailerIdLengthRule(32),
            GlobalLockerBatchRequiredCodeRule("global locker batch needs this field"),
            GlobalLockerBatchLocaleLengthRule(3600),
            GlobalLockerBatchRetriesRangeRule(64, 255),
        ),
    )
}
