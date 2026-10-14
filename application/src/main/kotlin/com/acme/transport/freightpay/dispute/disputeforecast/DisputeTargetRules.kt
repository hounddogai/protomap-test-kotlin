package com.acme.transport.freightpay.dispute.disputeforecast

/** A rule a dispute target must pass. Check returns its problems, or none. */
interface DisputeTargetRule {
    fun check(item: DisputeTarget): MutableList<String>
}

data class DisputeTargetRequiredTitleRule(var message: String) : DisputeTargetRule {
    override fun check(item: DisputeTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeTargetRequiredCurrencyRule(var message: String) : DisputeTargetRule {
    override fun check(item: DisputeTarget): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeTargetCodeLengthRule(var maxLength: Int) : DisputeTargetRule {
    override fun check(item: DisputeTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeTargetRequiredReviewerRule(var message: String) : DisputeTargetRule {
    override fun check(item: DisputeTarget): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeTargetRevisionRangeRule(var min: Int, var max: Int) : DisputeTargetRule {
    override fun check(item: DisputeTarget): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class DisputeTargetSequenceRangeRule(var min: Int, var max: Int) : DisputeTargetRule {
    override fun check(item: DisputeTarget): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute target. */
data class DisputeTargetValidator(var rules: MutableList<DisputeTargetRule>) {
    fun validate(item: DisputeTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeTargetValidator(): DisputeTargetValidator {
    return DisputeTargetValidator(
        mutableListOf<DisputeTargetRule>(
            DisputeTargetRequiredTitleRule("dispute target needs a value"),
            DisputeTargetRequiredCurrencyRule("dispute target needs this field"),
            DisputeTargetCodeLengthRule(1000),
            DisputeTargetRequiredReviewerRule("dispute target needs a value"),
            DisputeTargetRevisionRangeRule(32, 255),
            DisputeTargetSequenceRangeRule(32, 1000),
        ),
    )
}
