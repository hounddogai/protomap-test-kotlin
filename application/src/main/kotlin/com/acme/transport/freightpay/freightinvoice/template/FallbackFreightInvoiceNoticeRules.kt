package com.acme.transport.freightpay.freightinvoice.template

/** A rule a fallback freight invoice notice must pass. Check returns its problems, or none. */
interface FallbackFreightInvoiceNoticeRule {
    fun check(item: FallbackFreightInvoiceNotice): MutableList<String>
}

data class FallbackFreightInvoiceNoticeWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : FallbackFreightInvoiceNoticeRule {
    override fun check(item: FallbackFreightInvoiceNotice): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceNoticeRequiredVehicleIdRule(
    var message: String,
) : FallbackFreightInvoiceNoticeRule {
    override fun check(item: FallbackFreightInvoiceNotice): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceNoticeRequiredRegionRule(
    var message: String,
) : FallbackFreightInvoiceNoticeRule {
    override fun check(item: FallbackFreightInvoiceNotice): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceNoticeRequiredCodeRule(
    var message: String,
) : FallbackFreightInvoiceNoticeRule {
    override fun check(item: FallbackFreightInvoiceNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback freight invoice notice. */
data class FallbackFreightInvoiceNoticeValidator(var rules: MutableList<FallbackFreightInvoiceNoticeRule>) {
    fun validate(item: FallbackFreightInvoiceNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackFreightInvoiceNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackFreightInvoiceNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackFreightInvoiceNoticeValidator(): FallbackFreightInvoiceNoticeValidator {
    return FallbackFreightInvoiceNoticeValidator(
        mutableListOf<FallbackFreightInvoiceNoticeRule>(
            FallbackFreightInvoiceNoticeWindowDaysRangeRule(1000, 86400),
            FallbackFreightInvoiceNoticeRequiredVehicleIdRule("fallback freight invoice notice needs an entry"),
            FallbackFreightInvoiceNoticeRequiredRegionRule("fallback freight invoice notice needs a value"),
            FallbackFreightInvoiceNoticeRequiredCodeRule("fallback freight invoice notice needs this field"),
        ),
    )
}
