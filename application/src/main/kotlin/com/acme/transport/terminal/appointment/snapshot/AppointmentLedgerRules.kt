package com.acme.transport.terminal.appointment.snapshot

/** A rule a appointment ledger must pass. Check returns its problems, or none. */
interface AppointmentLedgerRule {
    fun check(item: AppointmentLedger): MutableList<String>
}

data class AppointmentLedgerRequiredCountryRule(var message: String) : AppointmentLedgerRule {
    override fun check(item: AppointmentLedger): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentLedgerRequiredNameRule(var message: String) : AppointmentLedgerRule {
    override fun check(item: AppointmentLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentLedgerCodeLengthRule(var maxLength: Int) : AppointmentLedgerRule {
    override fun check(item: AppointmentLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentLedgerRequiredCodeRule(var message: String) : AppointmentLedgerRule {
    override fun check(item: AppointmentLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentLedgerCountryLengthRule(var maxLength: Int) : AppointmentLedgerRule {
    override fun check(item: AppointmentLedger): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentLedgerVersionRangeRule(var min: Int, var max: Int) : AppointmentLedgerRule {
    override fun check(item: AppointmentLedger): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment ledger. */
data class AppointmentLedgerValidator(var rules: MutableList<AppointmentLedgerRule>) {
    fun validate(item: AppointmentLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentLedgerValidator(): AppointmentLedgerValidator {
    return AppointmentLedgerValidator(
        mutableListOf<AppointmentLedgerRule>(
            AppointmentLedgerRequiredCountryRule("appointment ledger needs this field"),
            AppointmentLedgerRequiredNameRule("appointment ledger needs an entry"),
            AppointmentLedgerCodeLengthRule(64),
            AppointmentLedgerRequiredCodeRule("appointment ledger needs a value"),
            AppointmentLedgerCountryLengthRule(255),
            AppointmentLedgerVersionRangeRule(120, 86400),
        ),
    )
}
