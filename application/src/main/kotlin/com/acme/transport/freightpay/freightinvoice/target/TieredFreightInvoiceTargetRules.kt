package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.kit.text.normalize

/** A rule a tiered freight invoice target must pass. Check returns its problems, or none. */
interface TieredFreightInvoiceTargetRule {
    fun check(item: TieredFreightInvoiceTarget): MutableList<String>
}

data class TieredFreightInvoiceTargetNameLengthRule(var maxLength: Int) : TieredFreightInvoiceTargetRule {
    override fun check(item: TieredFreightInvoiceTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceTargetAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : TieredFreightInvoiceTargetRule {
    override fun check(item: TieredFreightInvoiceTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceTargetCodePrefixRule(var prefix: String) : TieredFreightInvoiceTargetRule {
    override fun check(item: TieredFreightInvoiceTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceTargetApprovedGuardRule(var limit: Double) : TieredFreightInvoiceTargetRule {
    override fun check(item: TieredFreightInvoiceTarget): MutableList<String> {
        if (item.approved && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceTargetRequiredNameRule(var message: String) : TieredFreightInvoiceTargetRule {
    override fun check(item: TieredFreightInvoiceTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered freight invoice target. */
data class TieredFreightInvoiceTargetValidator(var rules: MutableList<TieredFreightInvoiceTargetRule>) {
    fun validate(item: TieredFreightInvoiceTarget): MutableList<String> {
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

    fun requireValid(item: TieredFreightInvoiceTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFreightInvoiceTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFreightInvoiceTargetValidator(): TieredFreightInvoiceTargetValidator {
    return TieredFreightInvoiceTargetValidator(
        mutableListOf<TieredFreightInvoiceTargetRule>(
            TieredFreightInvoiceTargetNameLengthRule(86400),
            TieredFreightInvoiceTargetAllowedTrailerIdRule(mutableListOf<String>("seasonal", "batched")),
            TieredFreightInvoiceTargetCodePrefixRule("OPS"),
            TieredFreightInvoiceTargetApprovedGuardRule(7.7),
            TieredFreightInvoiceTargetRequiredNameRule("tiered freight invoice target needs an entry"),
        ),
    )
}
