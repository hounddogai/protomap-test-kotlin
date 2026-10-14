package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import com.acme.transport.kit.text.normalize

/** A rule a default fuel surcharge entry must pass. Check returns its problems, or none. */
interface DefaultFuelSurchargeEntryRule {
    fun check(item: DefaultFuelSurchargeEntry): MutableList<String>
}

data class DefaultFuelSurchargeEntryAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : DefaultFuelSurchargeEntryRule {
    override fun check(item: DefaultFuelSurchargeEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeEntryLabelLengthRule(var maxLength: Int) : DefaultFuelSurchargeEntryRule {
    override fun check(item: DefaultFuelSurchargeEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeEntryTrailerIdLengthRule(var maxLength: Int) : DefaultFuelSurchargeEntryRule {
    override fun check(item: DefaultFuelSurchargeEntry): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeEntryRequiredTrailerIdRule(var message: String) : DefaultFuelSurchargeEntryRule {
    override fun check(item: DefaultFuelSurchargeEntry): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeEntryScoreRangeRule(
    var min: Double,
    var max: Double,
) : DefaultFuelSurchargeEntryRule {
    override fun check(item: DefaultFuelSurchargeEntry): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeEntryRequiredLabelRule(var message: String) : DefaultFuelSurchargeEntryRule {
    override fun check(item: DefaultFuelSurchargeEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default fuel surcharge entry. */
data class DefaultFuelSurchargeEntryValidator(var rules: MutableList<DefaultFuelSurchargeEntryRule>) {
    fun validate(item: DefaultFuelSurchargeEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultFuelSurchargeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultFuelSurchargeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultFuelSurchargeEntryValidator(): DefaultFuelSurchargeEntryValidator {
    return DefaultFuelSurchargeEntryValidator(
        mutableListOf<DefaultFuelSurchargeEntryRule>(
            DefaultFuelSurchargeEntryAllowedRouteCodeRule(mutableListOf<String>()),
            DefaultFuelSurchargeEntryLabelLengthRule(32),
            DefaultFuelSurchargeEntryTrailerIdLengthRule(120),
            DefaultFuelSurchargeEntryRequiredTrailerIdRule("default fuel surcharge entry needs a value"),
            DefaultFuelSurchargeEntryScoreRangeRule(5.5, 4162.3),
            DefaultFuelSurchargeEntryRequiredLabelRule("default fuel surcharge entry needs an entry"),
        ),
    )
}
