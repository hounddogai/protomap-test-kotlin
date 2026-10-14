package com.acme.transport.terminal.appointment.exception

/** A rule a primary appointment quota must pass. Check returns its problems, or none. */
interface PrimaryAppointmentQuotaRule {
    fun check(item: PrimaryAppointmentQuota): MutableList<String>
}

data class PrimaryAppointmentQuotaDockLengthRule(var maxLength: Int) : PrimaryAppointmentQuotaRule {
    override fun check(item: PrimaryAppointmentQuota): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentQuotaRequiredSummaryRule(var message: String) : PrimaryAppointmentQuotaRule {
    override fun check(item: PrimaryAppointmentQuota): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentQuotaCodeLengthRule(var maxLength: Int) : PrimaryAppointmentQuotaRule {
    override fun check(item: PrimaryAppointmentQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentQuotaRequiredDockRule(var message: String) : PrimaryAppointmentQuotaRule {
    override fun check(item: PrimaryAppointmentQuota): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentQuotaRequiredCodeRule(var message: String) : PrimaryAppointmentQuotaRule {
    override fun check(item: PrimaryAppointmentQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary appointment quota. */
data class PrimaryAppointmentQuotaValidator(var rules: MutableList<PrimaryAppointmentQuotaRule>) {
    fun validate(item: PrimaryAppointmentQuota): MutableList<String> {
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

    fun requireValid(item: PrimaryAppointmentQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAppointmentQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAppointmentQuotaValidator(): PrimaryAppointmentQuotaValidator {
    return PrimaryAppointmentQuotaValidator(
        mutableListOf<PrimaryAppointmentQuotaRule>(
            PrimaryAppointmentQuotaDockLengthRule(255),
            PrimaryAppointmentQuotaRequiredSummaryRule("primary appointment quota needs a value"),
            PrimaryAppointmentQuotaCodeLengthRule(1000),
            PrimaryAppointmentQuotaRequiredDockRule("primary appointment quota needs an entry"),
            PrimaryAppointmentQuotaRequiredCodeRule("primary appointment quota needs a value"),
        ),
    )
}
