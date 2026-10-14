package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.text.normalize

/** A rule a capped ev charge variance must pass. Check returns its problems, or none. */
interface CappedEvChargeVarianceRule {
    fun check(item: CappedEvChargeVariance): MutableList<String>
}

data class CappedEvChargeVarianceCodePrefixRule(var prefix: String) : CappedEvChargeVarianceRule {
    override fun check(item: CappedEvChargeVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeVarianceAllowedSummaryRule(
    var allowed: MutableList<String>,
) : CappedEvChargeVarianceRule {
    override fun check(item: CappedEvChargeVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeVarianceRequiredOwnerIdRule(var message: String) : CappedEvChargeVarianceRule {
    override fun check(item: CappedEvChargeVariance): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeVarianceGraceDaysRangeRule(var min: Int, var max: Int) : CappedEvChargeVarianceRule {
    override fun check(item: CappedEvChargeVariance): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeVariancePinnedGuardRule(var limit: Double) : CappedEvChargeVarianceRule {
    override fun check(item: CappedEvChargeVariance): MutableList<String> {
        if (item.pinned && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeVarianceRequiredVehicleIdRule(var message: String) : CappedEvChargeVarianceRule {
    override fun check(item: CappedEvChargeVariance): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped ev charge variance. */
data class CappedEvChargeVarianceValidator(var rules: MutableList<CappedEvChargeVarianceRule>) {
    fun validate(item: CappedEvChargeVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedEvChargeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedEvChargeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedEvChargeVarianceValidator(): CappedEvChargeVarianceValidator {
    return CappedEvChargeVarianceValidator(
        mutableListOf<CappedEvChargeVarianceRule>(
            CappedEvChargeVarianceCodePrefixRule("Q"),
            CappedEvChargeVarianceAllowedSummaryRule(mutableListOf<String>()),
            CappedEvChargeVarianceRequiredOwnerIdRule("capped ev charge variance needs a value"),
            CappedEvChargeVarianceGraceDaysRangeRule(64, 120),
            CappedEvChargeVariancePinnedGuardRule(2.8),
            CappedEvChargeVarianceRequiredVehicleIdRule("capped ev charge variance needs this field"),
        ),
    )
}
