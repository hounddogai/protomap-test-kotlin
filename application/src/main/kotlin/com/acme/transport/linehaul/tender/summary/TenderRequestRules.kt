package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.text.normalize

/** A rule a tender request must pass. Check returns its problems, or none. */
interface TenderRequestRule {
    fun check(item: TenderRequest): MutableList<String>
}

data class TenderRequestAllowedTitleRule(var allowed: MutableList<String>) : TenderRequestRule {
    override fun check(item: TenderRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class TenderRequestRequiredCodeRule(var message: String) : TenderRequestRule {
    override fun check(item: TenderRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderRequestEtaMinutesRangeRule(var min: Int, var max: Int) : TenderRequestRule {
    override fun check(item: TenderRequest): MutableList<String> {
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

data class TenderRequestRequiredLaneRule(var message: String) : TenderRequestRule {
    override fun check(item: TenderRequest): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderRequestTitleLengthRule(var maxLength: Int) : TenderRequestRule {
    override fun check(item: TenderRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderRequestRetriesRangeRule(var min: Int, var max: Int) : TenderRequestRule {
    override fun check(item: TenderRequest): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender request. */
data class TenderRequestValidator(var rules: MutableList<TenderRequestRule>) {
    fun validate(item: TenderRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderRequestValidator(): TenderRequestValidator {
    return TenderRequestValidator(
        mutableListOf<TenderRequestRule>(
            TenderRequestAllowedTitleRule(mutableListOf<String>("primary")),
            TenderRequestRequiredCodeRule("tender request needs this field"),
            TenderRequestEtaMinutesRangeRule(16, 64),
            TenderRequestRequiredLaneRule("tender request needs a value"),
            TenderRequestTitleLengthRule(16),
            TenderRequestRetriesRangeRule(64, 80),
        ),
    )
}
