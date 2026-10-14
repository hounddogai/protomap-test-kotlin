package com.acme.transport.terminal.appointment.seasonalappointmentvariance

/** A rule a pending appointment variance must pass. Check returns its problems, or none. */
interface PendingAppointmentVarianceRule {
    fun check(item: PendingAppointmentVariance): MutableList<String>
}

data class PendingAppointmentVarianceRequiredTitleRule(var message: String) : PendingAppointmentVarianceRule {
    override fun check(item: PendingAppointmentVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentVarianceRequiredNameRule(var message: String) : PendingAppointmentVarianceRule {
    override fun check(item: PendingAppointmentVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentVarianceRequiredCategoryRule(
    var message: String,
) : PendingAppointmentVarianceRule {
    override fun check(item: PendingAppointmentVariance): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentVarianceTitleLengthRule(var maxLength: Int) : PendingAppointmentVarianceRule {
    override fun check(item: PendingAppointmentVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentVarianceNameLengthRule(var maxLength: Int) : PendingAppointmentVarianceRule {
    override fun check(item: PendingAppointmentVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending appointment variance. */
data class PendingAppointmentVarianceValidator(var rules: MutableList<PendingAppointmentVarianceRule>) {
    fun validate(item: PendingAppointmentVariance): MutableList<String> {
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

    fun requireValid(item: PendingAppointmentVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAppointmentVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAppointmentVarianceValidator(): PendingAppointmentVarianceValidator {
    return PendingAppointmentVarianceValidator(
        mutableListOf<PendingAppointmentVarianceRule>(
            PendingAppointmentVarianceRequiredTitleRule("pending appointment variance needs an entry"),
            PendingAppointmentVarianceRequiredNameRule("pending appointment variance needs this field"),
            PendingAppointmentVarianceRequiredCategoryRule("pending appointment variance needs an entry"),
            PendingAppointmentVarianceTitleLengthRule(64),
            PendingAppointmentVarianceNameLengthRule(32),
        ),
    )
}
