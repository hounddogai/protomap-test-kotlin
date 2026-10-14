package com.acme.transport.freightpay.audit.rollup

/** A rule a tiered audit draft must pass. Check returns its problems, or none. */
interface TieredAuditDraftRule {
    fun check(item: TieredAuditDraft): MutableList<String>
}

data class TieredAuditDraftRequiredVehicleIdRule(var message: String) : TieredAuditDraftRule {
    override fun check(item: TieredAuditDraft): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditDraftLabelLengthRule(var maxLength: Int) : TieredAuditDraftRule {
    override fun check(item: TieredAuditDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditDraftCodePrefixRule(var prefix: String) : TieredAuditDraftRule {
    override fun check(item: TieredAuditDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered audit draft. */
data class TieredAuditDraftValidator(var rules: MutableList<TieredAuditDraftRule>) {
    fun validate(item: TieredAuditDraft): MutableList<String> {
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

    fun requireValid(item: TieredAuditDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAuditDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAuditDraftValidator(): TieredAuditDraftValidator {
    return TieredAuditDraftValidator(
        mutableListOf<TieredAuditDraftRule>(
            TieredAuditDraftRequiredVehicleIdRule("tiered audit draft needs an entry"),
            TieredAuditDraftLabelLengthRule(32),
            TieredAuditDraftCodePrefixRule("OPS"),
        ),
    )
}
