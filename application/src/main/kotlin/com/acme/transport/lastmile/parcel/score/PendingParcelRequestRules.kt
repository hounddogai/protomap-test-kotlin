package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.text.normalize

/** A rule a pending parcel request must pass. Check returns its problems, or none. */
interface PendingParcelRequestRule {
    fun check(item: PendingParcelRequest): MutableList<String>
}

data class PendingParcelRequestAllowedCodeRule(var allowed: MutableList<String>) : PendingParcelRequestRule {
    override fun check(item: PendingParcelRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PendingParcelRequestCodeLengthRule(var maxLength: Int) : PendingParcelRequestRule {
    override fun check(item: PendingParcelRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingParcelRequestBudgetRangeRule(var min: Double, var max: Double) : PendingParcelRequestRule {
    override fun check(item: PendingParcelRequest): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class PendingParcelRequestRequiredCodeRule(var message: String) : PendingParcelRequestRule {
    override fun check(item: PendingParcelRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingParcelRequestRetriesRangeRule(var min: Int, var max: Int) : PendingParcelRequestRule {
    override fun check(item: PendingParcelRequest): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class PendingParcelRequestNameLengthRule(var maxLength: Int) : PendingParcelRequestRule {
    override fun check(item: PendingParcelRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending parcel request. */
data class PendingParcelRequestValidator(var rules: MutableList<PendingParcelRequestRule>) {
    fun validate(item: PendingParcelRequest): MutableList<String> {
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

    fun requireValid(item: PendingParcelRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingParcelRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingParcelRequestValidator(): PendingParcelRequestValidator {
    return PendingParcelRequestValidator(
        mutableListOf<PendingParcelRequestRule>(
            PendingParcelRequestAllowedCodeRule(mutableListOf<String>("tiered")),
            PendingParcelRequestCodeLengthRule(120),
            PendingParcelRequestBudgetRangeRule(3.4, 152.0),
            PendingParcelRequestRequiredCodeRule("pending parcel request needs this field"),
            PendingParcelRequestRetriesRangeRule(255, 3600),
            PendingParcelRequestNameLengthRule(16),
        ),
    )
}
