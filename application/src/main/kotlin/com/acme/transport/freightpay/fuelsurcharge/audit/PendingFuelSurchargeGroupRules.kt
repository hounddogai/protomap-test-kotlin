package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.normalize

/** A rule a pending fuel surcharge group must pass. Check returns its problems, or none. */
interface PendingFuelSurchargeGroupRule {
    fun check(item: PendingFuelSurchargeGroup): MutableList<String>
}

data class PendingFuelSurchargeGroupOwnerIdLengthRule(var maxLength: Int) : PendingFuelSurchargeGroupRule {
    override fun check(item: PendingFuelSurchargeGroup): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeGroupApprovedGuardRule(var limit: Int) : PendingFuelSurchargeGroupRule {
    override fun check(item: PendingFuelSurchargeGroup): MutableList<String> {
        if (item.approved && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeGroupAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : PendingFuelSurchargeGroupRule {
    override fun check(item: PendingFuelSurchargeGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeGroupLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : PendingFuelSurchargeGroupRule {
    override fun check(item: PendingFuelSurchargeGroup): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelSurchargeGroupLabelLengthRule(var maxLength: Int) : PendingFuelSurchargeGroupRule {
    override fun check(item: PendingFuelSurchargeGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending fuel surcharge group. */
data class PendingFuelSurchargeGroupValidator(var rules: MutableList<PendingFuelSurchargeGroupRule>) {
    fun validate(item: PendingFuelSurchargeGroup): MutableList<String> {
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

    fun requireValid(item: PendingFuelSurchargeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFuelSurchargeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFuelSurchargeGroupValidator(): PendingFuelSurchargeGroupValidator {
    return PendingFuelSurchargeGroupValidator(
        mutableListOf<PendingFuelSurchargeGroupRule>(
            PendingFuelSurchargeGroupOwnerIdLengthRule(64),
            PendingFuelSurchargeGroupApprovedGuardRule(1000),
            PendingFuelSurchargeGroupAllowedOwnerIdRule(mutableListOf<String>("adaptive")),
            PendingFuelSurchargeGroupLoadKgRangeRule(8.1, 1476.3),
            PendingFuelSurchargeGroupLabelLengthRule(16),
        ),
    )
}
