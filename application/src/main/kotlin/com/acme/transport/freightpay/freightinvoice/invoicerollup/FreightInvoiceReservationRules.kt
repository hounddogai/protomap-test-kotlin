package com.acme.transport.freightpay.freightinvoice.invoicerollup

/** A rule a freight invoice reservation must pass. Check returns its problems, or none. */
interface FreightInvoiceReservationRule {
    fun check(item: FreightInvoiceReservation): MutableList<String>
}

data class FreightInvoiceReservationRequiredNameRule(var message: String) : FreightInvoiceReservationRule {
    override fun check(item: FreightInvoiceReservation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceReservationCodeLengthRule(var maxLength: Int) : FreightInvoiceReservationRule {
    override fun check(item: FreightInvoiceReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceReservationRequiredCodeRule(var message: String) : FreightInvoiceReservationRule {
    override fun check(item: FreightInvoiceReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceReservationPinnedGuardRule(var limit: Double) : FreightInvoiceReservationRule {
    override fun check(item: FreightInvoiceReservation): MutableList<String> {
        if (item.pinned && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice reservation. */
data class FreightInvoiceReservationValidator(var rules: MutableList<FreightInvoiceReservationRule>) {
    fun validate(item: FreightInvoiceReservation): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceReservationValidator(): FreightInvoiceReservationValidator {
    return FreightInvoiceReservationValidator(
        mutableListOf<FreightInvoiceReservationRule>(
            FreightInvoiceReservationRequiredNameRule("freight invoice reservation needs this field"),
            FreightInvoiceReservationCodeLengthRule(1000),
            FreightInvoiceReservationRequiredCodeRule("freight invoice reservation needs a value"),
            FreightInvoiceReservationPinnedGuardRule(2.1),
        ),
    )
}
