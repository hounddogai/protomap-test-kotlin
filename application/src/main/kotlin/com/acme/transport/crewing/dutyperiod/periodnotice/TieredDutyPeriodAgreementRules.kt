package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.text.normalize

/** A rule a tiered duty period agreement must pass. Check returns its problems, or none. */
interface TieredDutyPeriodAgreementRule {
    fun check(item: TieredDutyPeriodAgreement): MutableList<String>
}

data class TieredDutyPeriodAgreementAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : TieredDutyPeriodAgreementRule {
    override fun check(item: TieredDutyPeriodAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodAgreementRequiredCodeRule(var message: String) : TieredDutyPeriodAgreementRule {
    override fun check(item: TieredDutyPeriodAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodAgreementDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : TieredDutyPeriodAgreementRule {
    override fun check(item: TieredDutyPeriodAgreement): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodAgreementCodePrefixRule(var prefix: String) : TieredDutyPeriodAgreementRule {
    override fun check(item: TieredDutyPeriodAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered duty period agreement. */
data class TieredDutyPeriodAgreementValidator(var rules: MutableList<TieredDutyPeriodAgreementRule>) {
    fun validate(item: TieredDutyPeriodAgreement): MutableList<String> {
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

    fun requireValid(item: TieredDutyPeriodAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDutyPeriodAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDutyPeriodAgreementValidator(): TieredDutyPeriodAgreementValidator {
    return TieredDutyPeriodAgreementValidator(
        mutableListOf<TieredDutyPeriodAgreementRule>(
            TieredDutyPeriodAgreementAllowedOwnerIdRule(mutableListOf<String>("primary")),
            TieredDutyPeriodAgreementRequiredCodeRule("tiered duty period agreement needs an entry"),
            TieredDutyPeriodAgreementDistanceKmRangeRule(1.1, 4886.6),
            TieredDutyPeriodAgreementCodePrefixRule("X"),
        ),
    )
}
