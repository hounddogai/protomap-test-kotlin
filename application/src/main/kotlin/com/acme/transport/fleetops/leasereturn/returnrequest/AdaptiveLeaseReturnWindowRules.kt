package com.acme.transport.fleetops.leasereturn.returnrequest

import com.acme.transport.kit.text.normalize

/** A rule a adaptive lease return window must pass. Check returns its problems, or none. */
interface AdaptiveLeaseReturnWindowRule {
    fun check(item: AdaptiveLeaseReturnWindow): MutableList<String>
}

data class AdaptiveLeaseReturnWindowAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveLeaseReturnWindowRule {
    override fun check(item: AdaptiveLeaseReturnWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnWindowUnitLengthRule(var maxLength: Int) : AdaptiveLeaseReturnWindowRule {
    override fun check(item: AdaptiveLeaseReturnWindow): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnWindowRequiredUnitRule(var message: String) : AdaptiveLeaseReturnWindowRule {
    override fun check(item: AdaptiveLeaseReturnWindow): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnWindowRequiredCodeRule(var message: String) : AdaptiveLeaseReturnWindowRule {
    override fun check(item: AdaptiveLeaseReturnWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnWindowRequiredNameRule(var message: String) : AdaptiveLeaseReturnWindowRule {
    override fun check(item: AdaptiveLeaseReturnWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnWindowNameLengthRule(var maxLength: Int) : AdaptiveLeaseReturnWindowRule {
    override fun check(item: AdaptiveLeaseReturnWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lease return window. */
data class AdaptiveLeaseReturnWindowValidator(var rules: MutableList<AdaptiveLeaseReturnWindowRule>) {
    fun validate(item: AdaptiveLeaseReturnWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveLeaseReturnWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLeaseReturnWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLeaseReturnWindowValidator(): AdaptiveLeaseReturnWindowValidator {
    return AdaptiveLeaseReturnWindowValidator(
        mutableListOf<AdaptiveLeaseReturnWindowRule>(
            AdaptiveLeaseReturnWindowAllowedCodeRule(mutableListOf<String>("seasonal", "tiered")),
            AdaptiveLeaseReturnWindowUnitLengthRule(32),
            AdaptiveLeaseReturnWindowRequiredUnitRule("adaptive lease return window needs this field"),
            AdaptiveLeaseReturnWindowRequiredCodeRule("adaptive lease return window needs a value"),
            AdaptiveLeaseReturnWindowRequiredNameRule("adaptive lease return window needs an entry"),
            AdaptiveLeaseReturnWindowNameLengthRule(255),
        ),
    )
}
