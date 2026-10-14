package com.acme.transport.lastmile.parcel.quota

/** A rule a seasonal parcel quota must pass. Check returns its problems, or none. */
interface SeasonalParcelQuotaRule {
    fun check(item: SeasonalParcelQuota): MutableList<String>
}

data class SeasonalParcelQuotaTrailerIdLengthRule(var maxLength: Int) : SeasonalParcelQuotaRule {
    override fun check(item: SeasonalParcelQuota): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelQuotaRequiredChecksumRule(var message: String) : SeasonalParcelQuotaRule {
    override fun check(item: SeasonalParcelQuota): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelQuotaActiveGuardRule(var limit: Double) : SeasonalParcelQuotaRule {
    override fun check(item: SeasonalParcelQuota): MutableList<String> {
        if (item.active && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal parcel quota. */
data class SeasonalParcelQuotaValidator(var rules: MutableList<SeasonalParcelQuotaRule>) {
    fun validate(item: SeasonalParcelQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalParcelQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalParcelQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalParcelQuotaValidator(): SeasonalParcelQuotaValidator {
    return SeasonalParcelQuotaValidator(
        mutableListOf<SeasonalParcelQuotaRule>(
            SeasonalParcelQuotaTrailerIdLengthRule(32),
            SeasonalParcelQuotaRequiredChecksumRule("seasonal parcel quota needs this field"),
            SeasonalParcelQuotaActiveGuardRule(6.9),
        ),
    )
}
