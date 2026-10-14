package com.acme.transport.lastmile.redelivery.redeliveryrule

/** A rule a redelivery draft must pass. Check returns its problems, or none. */
interface RedeliveryDraftRule {
    fun check(item: RedeliveryDraft): MutableList<String>
}

data class RedeliveryDraftArchivedGuardRule(var limit: Int) : RedeliveryDraftRule {
    override fun check(item: RedeliveryDraft): MutableList<String> {
        if (item.archived && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryDraftEtaMinutesRangeRule(var min: Int, var max: Int) : RedeliveryDraftRule {
    override fun check(item: RedeliveryDraft): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryDraftCodeLengthRule(var maxLength: Int) : RedeliveryDraftRule {
    override fun check(item: RedeliveryDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryDraftOdometerRangeRule(var min: Int, var max: Int) : RedeliveryDraftRule {
    override fun check(item: RedeliveryDraft): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class RedeliveryDraftRequiredCodeRule(var message: String) : RedeliveryDraftRule {
    override fun check(item: RedeliveryDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery draft. */
data class RedeliveryDraftValidator(var rules: MutableList<RedeliveryDraftRule>) {
    fun validate(item: RedeliveryDraft): MutableList<String> {
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

    fun requireValid(item: RedeliveryDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryDraftValidator(): RedeliveryDraftValidator {
    return RedeliveryDraftValidator(
        mutableListOf<RedeliveryDraftRule>(
            RedeliveryDraftArchivedGuardRule(3600),
            RedeliveryDraftEtaMinutesRangeRule(16, 32),
            RedeliveryDraftCodeLengthRule(1000),
            RedeliveryDraftOdometerRangeRule(32, 32),
            RedeliveryDraftRequiredCodeRule("redelivery draft needs an entry"),
        ),
    )
}
