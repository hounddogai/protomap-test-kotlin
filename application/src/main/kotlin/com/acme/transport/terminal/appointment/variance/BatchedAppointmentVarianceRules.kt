package com.acme.transport.terminal.appointment.variance

/** A rule a batched appointment variance must pass. Check returns its problems, or none. */
interface BatchedAppointmentVarianceRule {
    fun check(item: BatchedAppointmentVariance): MutableList<String>
}

data class BatchedAppointmentVarianceRequiredCategoryRule(
    var message: String,
) : BatchedAppointmentVarianceRule {
    override fun check(item: BatchedAppointmentVariance): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentVarianceTrailerIdLengthRule(var maxLength: Int) : BatchedAppointmentVarianceRule {
    override fun check(item: BatchedAppointmentVariance): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentVarianceLabelLengthRule(var maxLength: Int) : BatchedAppointmentVarianceRule {
    override fun check(item: BatchedAppointmentVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched appointment variance. */
data class BatchedAppointmentVarianceValidator(var rules: MutableList<BatchedAppointmentVarianceRule>) {
    fun validate(item: BatchedAppointmentVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAppointmentVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAppointmentVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAppointmentVarianceValidator(): BatchedAppointmentVarianceValidator {
    return BatchedAppointmentVarianceValidator(
        mutableListOf<BatchedAppointmentVarianceRule>(
            BatchedAppointmentVarianceRequiredCategoryRule("batched appointment variance needs a value"),
            BatchedAppointmentVarianceTrailerIdLengthRule(80),
            BatchedAppointmentVarianceLabelLengthRule(86400),
        ),
    )
}
