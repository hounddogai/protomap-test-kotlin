package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.text.normalize

/** A rule a locker claim must pass. Check returns its problems, or none. */
interface LockerClaimRule {
    fun check(item: LockerClaim): MutableList<String>
}

data class LockerClaimWindowDaysRangeRule(var min: Int, var max: Int) : LockerClaimRule {
    override fun check(item: LockerClaim): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class LockerClaimLabelLengthRule(var maxLength: Int) : LockerClaimRule {
    override fun check(item: LockerClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerClaimRequiredLabelRule(var message: String) : LockerClaimRule {
    override fun check(item: LockerClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerClaimDiscountRangeRule(var min: Double, var max: Double) : LockerClaimRule {
    override fun check(item: LockerClaim): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerClaimAllowedLabelRule(var allowed: MutableList<String>) : LockerClaimRule {
    override fun check(item: LockerClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker claim. */
data class LockerClaimValidator(var rules: MutableList<LockerClaimRule>) {
    fun validate(item: LockerClaim): MutableList<String> {
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

    fun requireValid(item: LockerClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerClaimValidator(): LockerClaimValidator {
    return LockerClaimValidator(
        mutableListOf<LockerClaimRule>(
            LockerClaimWindowDaysRangeRule(16, 86400),
            LockerClaimLabelLengthRule(64),
            LockerClaimRequiredLabelRule("locker claim needs an entry"),
            LockerClaimDiscountRangeRule(1.2, 3856.8),
            LockerClaimAllowedLabelRule(mutableListOf<String>("seasonal", "rolling")),
        ),
    )
}
