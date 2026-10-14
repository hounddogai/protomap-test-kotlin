package com.acme.transport.customs.tariff.entry

/** A rule a tariff digest must pass. Check returns its problems, or none. */
interface TariffDigestRule {
    fun check(item: TariffDigest): MutableList<String>
}

data class TariffDigestTitleLengthRule(var maxLength: Int) : TariffDigestRule {
    override fun check(item: TariffDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffDigestActiveGuardRule(var limit: Int) : TariffDigestRule {
    override fun check(item: TariffDigest): MutableList<String> {
        if (item.active && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class TariffDigestReviewerLengthRule(var maxLength: Int) : TariffDigestRule {
    override fun check(item: TariffDigest): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff digest. */
data class TariffDigestValidator(var rules: MutableList<TariffDigestRule>) {
    fun validate(item: TariffDigest): MutableList<String> {
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

    fun requireValid(item: TariffDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffDigestValidator(): TariffDigestValidator {
    return TariffDigestValidator(
        mutableListOf<TariffDigestRule>(
            TariffDigestTitleLengthRule(64),
            TariffDigestActiveGuardRule(3600),
            TariffDigestReviewerLengthRule(32),
        ),
    )
}
