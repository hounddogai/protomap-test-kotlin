package com.acme.transport.freightpay.accessorial.draft

/** A rule a scheduled accessorial batch must pass. Check returns its problems, or none. */
interface ScheduledAccessorialBatchRule {
    fun check(item: ScheduledAccessorialBatch): MutableList<String>
}

data class ScheduledAccessorialBatchCapacityRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAccessorialBatchRule {
    override fun check(item: ScheduledAccessorialBatch): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class ScheduledAccessorialBatchNameLengthRule(var maxLength: Int) : ScheduledAccessorialBatchRule {
    override fun check(item: ScheduledAccessorialBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialBatchCodePrefixRule(var prefix: String) : ScheduledAccessorialBatchRule {
    override fun check(item: ScheduledAccessorialBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialBatchOdometerRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAccessorialBatchRule {
    override fun check(item: ScheduledAccessorialBatch): MutableList<String> {
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

data class ScheduledAccessorialBatchCodeLengthRule(var maxLength: Int) : ScheduledAccessorialBatchRule {
    override fun check(item: ScheduledAccessorialBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled accessorial batch. */
data class ScheduledAccessorialBatchValidator(var rules: MutableList<ScheduledAccessorialBatchRule>) {
    fun validate(item: ScheduledAccessorialBatch): MutableList<String> {
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

    fun requireValid(item: ScheduledAccessorialBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAccessorialBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAccessorialBatchValidator(): ScheduledAccessorialBatchValidator {
    return ScheduledAccessorialBatchValidator(
        mutableListOf<ScheduledAccessorialBatchRule>(
            ScheduledAccessorialBatchCapacityRangeRule(255, 1000),
            ScheduledAccessorialBatchNameLengthRule(80),
            ScheduledAccessorialBatchCodePrefixRule("OPS"),
            ScheduledAccessorialBatchOdometerRangeRule(255, 1000),
            ScheduledAccessorialBatchCodeLengthRule(80),
        ),
    )
}
