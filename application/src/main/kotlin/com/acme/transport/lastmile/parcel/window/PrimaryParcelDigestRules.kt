package com.acme.transport.lastmile.parcel.window

/** A rule a primary parcel digest must pass. Check returns its problems, or none. */
interface PrimaryParcelDigestRule {
    fun check(item: PrimaryParcelDigest): MutableList<String>
}

data class PrimaryParcelDigestEtaMinutesRangeRule(var min: Int, var max: Int) : PrimaryParcelDigestRule {
    override fun check(item: PrimaryParcelDigest): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class PrimaryParcelDigestArchivedGuardRule(var limit: Int) : PrimaryParcelDigestRule {
    override fun check(item: PrimaryParcelDigest): MutableList<String> {
        if (item.archived && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryParcelDigestRequiredNameRule(var message: String) : PrimaryParcelDigestRule {
    override fun check(item: PrimaryParcelDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryParcelDigestRequiredCurrencyRule(var message: String) : PrimaryParcelDigestRule {
    override fun check(item: PrimaryParcelDigest): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary parcel digest. */
data class PrimaryParcelDigestValidator(var rules: MutableList<PrimaryParcelDigestRule>) {
    fun validate(item: PrimaryParcelDigest): MutableList<String> {
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

    fun requireValid(item: PrimaryParcelDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryParcelDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryParcelDigestValidator(): PrimaryParcelDigestValidator {
    return PrimaryParcelDigestValidator(
        mutableListOf<PrimaryParcelDigestRule>(
            PrimaryParcelDigestEtaMinutesRangeRule(80, 86400),
            PrimaryParcelDigestArchivedGuardRule(120),
            PrimaryParcelDigestRequiredNameRule("primary parcel digest needs an entry"),
            PrimaryParcelDigestRequiredCurrencyRule("primary parcel digest needs an entry"),
        ),
    )
}
