package com.acme.transport.freightpay.freightinvoice.profile

/** A rule a freight invoice profile must pass. Check returns its problems, or none. */
interface FreightInvoiceProfileRule {
    fun check(item: FreightInvoiceProfile): MutableList<String>
}

data class FreightInvoiceProfileDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : FreightInvoiceProfileRule {
    override fun check(item: FreightInvoiceProfile): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceProfileTitleLengthRule(var maxLength: Int) : FreightInvoiceProfileRule {
    override fun check(item: FreightInvoiceProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceProfileDepotLengthRule(var maxLength: Int) : FreightInvoiceProfileRule {
    override fun check(item: FreightInvoiceProfile): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceProfileOwnerIdLengthRule(var maxLength: Int) : FreightInvoiceProfileRule {
    override fun check(item: FreightInvoiceProfile): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice profile. */
data class FreightInvoiceProfileValidator(var rules: MutableList<FreightInvoiceProfileRule>) {
    fun validate(item: FreightInvoiceProfile): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceProfileValidator(): FreightInvoiceProfileValidator {
    return FreightInvoiceProfileValidator(
        mutableListOf<FreightInvoiceProfileRule>(
            FreightInvoiceProfileDurationMinutesRangeRule(80, 120),
            FreightInvoiceProfileTitleLengthRule(255),
            FreightInvoiceProfileDepotLengthRule(120),
            FreightInvoiceProfileOwnerIdLengthRule(86400),
        ),
    )
}
