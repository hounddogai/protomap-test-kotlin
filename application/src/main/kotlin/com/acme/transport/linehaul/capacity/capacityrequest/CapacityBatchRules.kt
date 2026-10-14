package com.acme.transport.linehaul.capacity.capacityrequest

/** A rule a capacity batch must pass. Check returns its problems, or none. */
interface CapacityBatchRule {
    fun check(item: CapacityBatch): MutableList<String>
}

data class CapacityBatchTitleLengthRule(var maxLength: Int) : CapacityBatchRule {
    override fun check(item: CapacityBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityBatchCodeLengthRule(var maxLength: Int) : CapacityBatchRule {
    override fun check(item: CapacityBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityBatchRequiredTitleRule(var message: String) : CapacityBatchRule {
    override fun check(item: CapacityBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity batch. */
data class CapacityBatchValidator(var rules: MutableList<CapacityBatchRule>) {
    fun validate(item: CapacityBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityBatchValidator(): CapacityBatchValidator {
    return CapacityBatchValidator(
        mutableListOf<CapacityBatchRule>(
            CapacityBatchTitleLengthRule(80),
            CapacityBatchCodeLengthRule(1000),
            CapacityBatchRequiredTitleRule("capacity batch needs an entry"),
        ),
    )
}
