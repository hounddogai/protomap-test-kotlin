package com.acme.transport.linehaul.laneplan.template

/** A rule a lane plan batch must pass. Check returns its problems, or none. */
interface LanePlanBatchRule {
    fun check(item: LanePlanBatch): MutableList<String>
}

data class LanePlanBatchRequiredCodeRule(var message: String) : LanePlanBatchRule {
    override fun check(item: LanePlanBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBatchRequiredLabelRule(var message: String) : LanePlanBatchRule {
    override fun check(item: LanePlanBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBatchCodePrefixRule(var prefix: String) : LanePlanBatchRule {
    override fun check(item: LanePlanBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBatchCodeLengthRule(var maxLength: Int) : LanePlanBatchRule {
    override fun check(item: LanePlanBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBatchStopCountRangeRule(var min: Int, var max: Int) : LanePlanBatchRule {
    override fun check(item: LanePlanBatch): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class LanePlanBatchRevisionRangeRule(var min: Int, var max: Int) : LanePlanBatchRule {
    override fun check(item: LanePlanBatch): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan batch. */
data class LanePlanBatchValidator(var rules: MutableList<LanePlanBatchRule>) {
    fun validate(item: LanePlanBatch): MutableList<String> {
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

    fun requireValid(item: LanePlanBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanBatchValidator(): LanePlanBatchValidator {
    return LanePlanBatchValidator(
        mutableListOf<LanePlanBatchRule>(
            LanePlanBatchRequiredCodeRule("lane plan batch needs an entry"),
            LanePlanBatchRequiredLabelRule("lane plan batch needs an entry"),
            LanePlanBatchCodePrefixRule("OPS"),
            LanePlanBatchCodeLengthRule(255),
            LanePlanBatchStopCountRangeRule(64, 80),
            LanePlanBatchRevisionRangeRule(255, 1000),
        ),
    )
}
