package com.acme.transport.safety.violation.manifest

/** A rule a manual violation batch must pass. Check returns its problems, or none. */
interface ManualViolationBatchRule {
    fun check(item: ManualViolationBatch): MutableList<String>
}

data class ManualViolationBatchLabelLengthRule(var maxLength: Int) : ManualViolationBatchRule {
    override fun check(item: ManualViolationBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationBatchCodeLengthRule(var maxLength: Int) : ManualViolationBatchRule {
    override fun check(item: ManualViolationBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationBatchTimezoneLengthRule(var maxLength: Int) : ManualViolationBatchRule {
    override fun check(item: ManualViolationBatch): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationBatchDiscountRangeRule(var min: Double, var max: Double) : ManualViolationBatchRule {
    override fun check(item: ManualViolationBatch): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class ManualViolationBatchRequiredLabelRule(var message: String) : ManualViolationBatchRule {
    override fun check(item: ManualViolationBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual violation batch. */
data class ManualViolationBatchValidator(var rules: MutableList<ManualViolationBatchRule>) {
    fun validate(item: ManualViolationBatch): MutableList<String> {
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

    fun requireValid(item: ManualViolationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualViolationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultManualViolationBatchValidator(): ManualViolationBatchValidator {
    return ManualViolationBatchValidator(
        mutableListOf<ManualViolationBatchRule>(
            ManualViolationBatchLabelLengthRule(120),
            ManualViolationBatchCodeLengthRule(86400),
            ManualViolationBatchTimezoneLengthRule(1000),
            ManualViolationBatchDiscountRangeRule(6.2, 1909.2),
            ManualViolationBatchRequiredLabelRule("manual violation batch needs a value"),
        ),
    )
}
