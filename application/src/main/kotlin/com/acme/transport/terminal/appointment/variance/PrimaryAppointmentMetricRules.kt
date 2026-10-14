package com.acme.transport.terminal.appointment.variance

/** A rule a primary appointment metric must pass. Check returns its problems, or none. */
interface PrimaryAppointmentMetricRule {
    fun check(item: PrimaryAppointmentMetric): MutableList<String>
}

data class PrimaryAppointmentMetricLocaleLengthRule(var maxLength: Int) : PrimaryAppointmentMetricRule {
    override fun check(item: PrimaryAppointmentMetric): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentMetricCodeLengthRule(var maxLength: Int) : PrimaryAppointmentMetricRule {
    override fun check(item: PrimaryAppointmentMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentMetricRequiredCodeRule(var message: String) : PrimaryAppointmentMetricRule {
    override fun check(item: PrimaryAppointmentMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary appointment metric. */
data class PrimaryAppointmentMetricValidator(var rules: MutableList<PrimaryAppointmentMetricRule>) {
    fun validate(item: PrimaryAppointmentMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAppointmentMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAppointmentMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAppointmentMetricValidator(): PrimaryAppointmentMetricValidator {
    return PrimaryAppointmentMetricValidator(
        mutableListOf<PrimaryAppointmentMetricRule>(
            PrimaryAppointmentMetricLocaleLengthRule(3600),
            PrimaryAppointmentMetricCodeLengthRule(1000),
            PrimaryAppointmentMetricRequiredCodeRule("primary appointment metric needs this field"),
        ),
    )
}
