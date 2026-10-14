package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge target must pass. Check returns its problems, or none. */
interface FuelSurchargeTargetRule {
    fun check(item: FuelSurchargeTarget): MutableList<String>
}

data class FuelSurchargeTargetNameLengthRule(var maxLength: Int) : FuelSurchargeTargetRule {
    override fun check(item: FuelSurchargeTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeTargetRequiredNameRule(var message: String) : FuelSurchargeTargetRule {
    override fun check(item: FuelSurchargeTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeTargetCodePrefixRule(var prefix: String) : FuelSurchargeTargetRule {
    override fun check(item: FuelSurchargeTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeTargetAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : FuelSurchargeTargetRule {
    override fun check(item: FuelSurchargeTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeTargetRequiredContactEmailRule(var message: String) : FuelSurchargeTargetRule {
    override fun check(item: FuelSurchargeTarget): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge target. */
data class FuelSurchargeTargetValidator(var rules: MutableList<FuelSurchargeTargetRule>) {
    fun validate(item: FuelSurchargeTarget): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeTargetValidator(): FuelSurchargeTargetValidator {
    return FuelSurchargeTargetValidator(
        mutableListOf<FuelSurchargeTargetRule>(
            FuelSurchargeTargetNameLengthRule(3600),
            FuelSurchargeTargetRequiredNameRule("fuel surcharge target needs an entry"),
            FuelSurchargeTargetCodePrefixRule("Q"),
            FuelSurchargeTargetAllowedDescriptionRule(mutableListOf<String>()),
            FuelSurchargeTargetRequiredContactEmailRule("fuel surcharge target needs this field"),
        ),
    )
}
