package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.normalize

/** A rule a capped transit time notice must pass. Check returns its problems, or none. */
interface CappedTransitTimeNoticeRule {
    fun check(item: CappedTransitTimeNotice): MutableList<String>
}

data class CappedTransitTimeNoticeArchivedGuardRule(var limit: Double) : CappedTransitTimeNoticeRule {
    override fun check(item: CappedTransitTimeNotice): MutableList<String> {
        if (item.archived && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class CappedTransitTimeNoticeRequiredTitleRule(var message: String) : CappedTransitTimeNoticeRule {
    override fun check(item: CappedTransitTimeNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTransitTimeNoticeRequiredCodeRule(var message: String) : CappedTransitTimeNoticeRule {
    override fun check(item: CappedTransitTimeNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTransitTimeNoticeAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : CappedTransitTimeNoticeRule {
    override fun check(item: CappedTransitTimeNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CappedTransitTimeNoticeTitleLengthRule(var maxLength: Int) : CappedTransitTimeNoticeRule {
    override fun check(item: CappedTransitTimeNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTransitTimeNoticeMinimumRangeRule(
    var min: Double,
    var max: Double,
) : CappedTransitTimeNoticeRule {
    override fun check(item: CappedTransitTimeNotice): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped transit time notice. */
data class CappedTransitTimeNoticeValidator(var rules: MutableList<CappedTransitTimeNoticeRule>) {
    fun validate(item: CappedTransitTimeNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTransitTimeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTransitTimeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTransitTimeNoticeValidator(): CappedTransitTimeNoticeValidator {
    return CappedTransitTimeNoticeValidator(
        mutableListOf<CappedTransitTimeNoticeRule>(
            CappedTransitTimeNoticeArchivedGuardRule(1.7),
            CappedTransitTimeNoticeRequiredTitleRule("capped transit time notice needs a value"),
            CappedTransitTimeNoticeRequiredCodeRule("capped transit time notice needs an entry"),
            CappedTransitTimeNoticeAllowedVehicleIdRule(mutableListOf<String>()),
            CappedTransitTimeNoticeTitleLengthRule(1000),
            CappedTransitTimeNoticeMinimumRangeRule(4.6, 1769.5),
        ),
    )
}
