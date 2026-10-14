package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

/** A rule a primary rate card draft must pass. Check returns its problems, or none. */
interface PrimaryRateCardDraftRule {
    fun check(item: PrimaryRateCardDraft): MutableList<String>
}

data class PrimaryRateCardDraftNameLengthRule(var maxLength: Int) : PrimaryRateCardDraftRule {
    override fun check(item: PrimaryRateCardDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRateCardDraftRequiredCodeRule(var message: String) : PrimaryRateCardDraftRule {
    override fun check(item: PrimaryRateCardDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRateCardDraftRequiredNameRule(var message: String) : PrimaryRateCardDraftRule {
    override fun check(item: PrimaryRateCardDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRateCardDraftRequiredDescriptionRule(var message: String) : PrimaryRateCardDraftRule {
    override fun check(item: PrimaryRateCardDraft): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRateCardDraftEndsAtRangeRule(var min: Int, var max: Int) : PrimaryRateCardDraftRule {
    override fun check(item: PrimaryRateCardDraft): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary rate card draft. */
data class PrimaryRateCardDraftValidator(var rules: MutableList<PrimaryRateCardDraftRule>) {
    fun validate(item: PrimaryRateCardDraft): MutableList<String> {
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

    fun requireValid(item: PrimaryRateCardDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRateCardDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRateCardDraftValidator(): PrimaryRateCardDraftValidator {
    return PrimaryRateCardDraftValidator(
        mutableListOf<PrimaryRateCardDraftRule>(
            PrimaryRateCardDraftNameLengthRule(64),
            PrimaryRateCardDraftRequiredCodeRule("primary rate card draft needs a value"),
            PrimaryRateCardDraftRequiredNameRule("primary rate card draft needs a value"),
            PrimaryRateCardDraftRequiredDescriptionRule("primary rate card draft needs an entry"),
            PrimaryRateCardDraftEndsAtRangeRule(64, 3600),
        ),
    )
}
