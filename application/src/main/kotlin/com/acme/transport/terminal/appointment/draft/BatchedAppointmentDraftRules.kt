package com.acme.transport.terminal.appointment.draft

/** A rule a batched appointment draft must pass. Check returns its problems, or none. */
interface BatchedAppointmentDraftRule {
    fun check(item: BatchedAppointmentDraft): MutableList<String>
}

data class BatchedAppointmentDraftCodePrefixRule(var prefix: String) : BatchedAppointmentDraftRule {
    override fun check(item: BatchedAppointmentDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentDraftThresholdRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAppointmentDraftRule {
    override fun check(item: BatchedAppointmentDraft): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class BatchedAppointmentDraftRequiredNameRule(var message: String) : BatchedAppointmentDraftRule {
    override fun check(item: BatchedAppointmentDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentDraftReviewerLengthRule(var maxLength: Int) : BatchedAppointmentDraftRule {
    override fun check(item: BatchedAppointmentDraft): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentDraftUpdatedAtRangeRule(var min: Int, var max: Int) : BatchedAppointmentDraftRule {
    override fun check(item: BatchedAppointmentDraft): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched appointment draft. */
data class BatchedAppointmentDraftValidator(var rules: MutableList<BatchedAppointmentDraftRule>) {
    fun validate(item: BatchedAppointmentDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAppointmentDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAppointmentDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAppointmentDraftValidator(): BatchedAppointmentDraftValidator {
    return BatchedAppointmentDraftValidator(
        mutableListOf<BatchedAppointmentDraftRule>(
            BatchedAppointmentDraftCodePrefixRule("OPS"),
            BatchedAppointmentDraftThresholdRangeRule(2.2, 3681.1),
            BatchedAppointmentDraftRequiredNameRule("batched appointment draft needs an entry"),
            BatchedAppointmentDraftReviewerLengthRule(1000),
            BatchedAppointmentDraftUpdatedAtRangeRule(80, 1000),
        ),
    )
}
