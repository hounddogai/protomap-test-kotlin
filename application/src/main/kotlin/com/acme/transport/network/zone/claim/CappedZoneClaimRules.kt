package com.acme.transport.network.zone.claim

/** A rule a capped zone claim must pass. Check returns its problems, or none. */
interface CappedZoneClaimRule {
    fun check(item: CappedZoneClaim): MutableList<String>
}

data class CappedZoneClaimRequiredTrailerIdRule(var message: String) : CappedZoneClaimRule {
    override fun check(item: CappedZoneClaim): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneClaimMinimumRangeRule(var min: Double, var max: Double) : CappedZoneClaimRule {
    override fun check(item: CappedZoneClaim): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class CappedZoneClaimCurrencyLengthRule(var maxLength: Int) : CappedZoneClaimRule {
    override fun check(item: CappedZoneClaim): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneClaimTrailerIdLengthRule(var maxLength: Int) : CappedZoneClaimRule {
    override fun check(item: CappedZoneClaim): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneClaimNameLengthRule(var maxLength: Int) : CappedZoneClaimRule {
    override fun check(item: CappedZoneClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped zone claim. */
data class CappedZoneClaimValidator(var rules: MutableList<CappedZoneClaimRule>) {
    fun validate(item: CappedZoneClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedZoneClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedZoneClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedZoneClaimValidator(): CappedZoneClaimValidator {
    return CappedZoneClaimValidator(
        mutableListOf<CappedZoneClaimRule>(
            CappedZoneClaimRequiredTrailerIdRule("capped zone claim needs an entry"),
            CappedZoneClaimMinimumRangeRule(2.0, 2045.8),
            CappedZoneClaimCurrencyLengthRule(16),
            CappedZoneClaimTrailerIdLengthRule(86400),
            CappedZoneClaimNameLengthRule(3600),
        ),
    )
}
