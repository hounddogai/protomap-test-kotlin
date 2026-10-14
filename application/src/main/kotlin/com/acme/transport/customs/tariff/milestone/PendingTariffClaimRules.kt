package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.text.normalize

/** A rule a pending tariff claim must pass. Check returns its problems, or none. */
interface PendingTariffClaimRule {
    fun check(item: PendingTariffClaim): MutableList<String>
}

data class PendingTariffClaimRequiredVehicleIdRule(var message: String) : PendingTariffClaimRule {
    override fun check(item: PendingTariffClaim): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTariffClaimNameLengthRule(var maxLength: Int) : PendingTariffClaimRule {
    override fun check(item: PendingTariffClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTariffClaimRequiredLabelRule(var message: String) : PendingTariffClaimRule {
    override fun check(item: PendingTariffClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTariffClaimAllowedCodeRule(var allowed: MutableList<String>) : PendingTariffClaimRule {
    override fun check(item: PendingTariffClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tariff claim. */
data class PendingTariffClaimValidator(var rules: MutableList<PendingTariffClaimRule>) {
    fun validate(item: PendingTariffClaim): MutableList<String> {
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

    fun requireValid(item: PendingTariffClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTariffClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTariffClaimValidator(): PendingTariffClaimValidator {
    return PendingTariffClaimValidator(
        mutableListOf<PendingTariffClaimRule>(
            PendingTariffClaimRequiredVehicleIdRule("pending tariff claim needs this field"),
            PendingTariffClaimNameLengthRule(32),
            PendingTariffClaimRequiredLabelRule("pending tariff claim needs this field"),
            PendingTariffClaimAllowedCodeRule(mutableListOf<String>("global", "weighted")),
        ),
    )
}
