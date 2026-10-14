package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.text.normalize

/** A rule a ev charge review must pass. Check returns its problems, or none. */
interface EvChargeReviewRule {
    fun check(item: EvChargeReview): MutableList<String>
}

data class EvChargeReviewCodeLengthRule(var maxLength: Int) : EvChargeReviewRule {
    override fun check(item: EvChargeReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeReviewNotesLengthRule(var maxLength: Int) : EvChargeReviewRule {
    override fun check(item: EvChargeReview): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeReviewRequiredNotesRule(var message: String) : EvChargeReviewRule {
    override fun check(item: EvChargeReview): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeReviewAllowedNameRule(var allowed: MutableList<String>) : EvChargeReviewRule {
    override fun check(item: EvChargeReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeReviewNameLengthRule(var maxLength: Int) : EvChargeReviewRule {
    override fun check(item: EvChargeReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge review. */
data class EvChargeReviewValidator(var rules: MutableList<EvChargeReviewRule>) {
    fun validate(item: EvChargeReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeReview>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeReviewValidator(): EvChargeReviewValidator {
    return EvChargeReviewValidator(
        mutableListOf<EvChargeReviewRule>(
            EvChargeReviewCodeLengthRule(80),
            EvChargeReviewNotesLengthRule(16),
            EvChargeReviewRequiredNotesRule("ev charge review needs a value"),
            EvChargeReviewAllowedNameRule(mutableListOf<String>("capped", "lenient", "primary")),
            EvChargeReviewNameLengthRule(16),
        ),
    )
}
