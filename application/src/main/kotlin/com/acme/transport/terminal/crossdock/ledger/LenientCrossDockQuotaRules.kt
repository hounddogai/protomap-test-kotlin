package com.acme.transport.terminal.crossdock.ledger

/** A rule a lenient cross dock quota must pass. Check returns its problems, or none. */
interface LenientCrossDockQuotaRule {
    fun check(item: LenientCrossDockQuota): MutableList<String>
}

data class LenientCrossDockQuotaFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : LenientCrossDockQuotaRule {
    override fun check(item: LenientCrossDockQuota): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class LenientCrossDockQuotaRegionLengthRule(var maxLength: Int) : LenientCrossDockQuotaRule {
    override fun check(item: LenientCrossDockQuota): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockQuotaCodePrefixRule(var prefix: String) : LenientCrossDockQuotaRule {
    override fun check(item: LenientCrossDockQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockQuotaRequiredRegionRule(var message: String) : LenientCrossDockQuotaRule {
    override fun check(item: LenientCrossDockQuota): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockQuotaNameLengthRule(var maxLength: Int) : LenientCrossDockQuotaRule {
    override fun check(item: LenientCrossDockQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockQuotaPalletsRangeRule(var min: Int, var max: Int) : LenientCrossDockQuotaRule {
    override fun check(item: LenientCrossDockQuota): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient cross dock quota. */
data class LenientCrossDockQuotaValidator(var rules: MutableList<LenientCrossDockQuotaRule>) {
    fun validate(item: LenientCrossDockQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientCrossDockQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCrossDockQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCrossDockQuotaValidator(): LenientCrossDockQuotaValidator {
    return LenientCrossDockQuotaValidator(
        mutableListOf<LenientCrossDockQuotaRule>(
            LenientCrossDockQuotaFloorValueRangeRule(5.3, 4079.2),
            LenientCrossDockQuotaRegionLengthRule(80),
            LenientCrossDockQuotaCodePrefixRule("X"),
            LenientCrossDockQuotaRequiredRegionRule("lenient cross dock quota needs this field"),
            LenientCrossDockQuotaNameLengthRule(32),
            LenientCrossDockQuotaPalletsRangeRule(32, 1000),
        ),
    )
}
