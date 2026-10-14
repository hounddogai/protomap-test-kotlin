package com.acme.transport.emissions.evcharge.agreement

/** A rule a ev charge agreement must pass. Check returns its problems, or none. */
interface EvChargeAgreementRule {
    fun check(item: EvChargeAgreement): MutableList<String>
}

data class EvChargeAgreementRequiredCodeRule(var message: String) : EvChargeAgreementRule {
    override fun check(item: EvChargeAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAgreementVisibleGuardRule(var limit: Int) : EvChargeAgreementRule {
    override fun check(item: EvChargeAgreement): MutableList<String> {
        if (item.visible && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAgreementTitleLengthRule(var maxLength: Int) : EvChargeAgreementRule {
    override fun check(item: EvChargeAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAgreementTrailerIdLengthRule(var maxLength: Int) : EvChargeAgreementRule {
    override fun check(item: EvChargeAgreement): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAgreementScoreRangeRule(var min: Double, var max: Double) : EvChargeAgreementRule {
    override fun check(item: EvChargeAgreement): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class EvChargeAgreementBatchSizeRangeRule(var min: Int, var max: Int) : EvChargeAgreementRule {
    override fun check(item: EvChargeAgreement): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge agreement. */
data class EvChargeAgreementValidator(var rules: MutableList<EvChargeAgreementRule>) {
    fun validate(item: EvChargeAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeAgreementValidator(): EvChargeAgreementValidator {
    return EvChargeAgreementValidator(
        mutableListOf<EvChargeAgreementRule>(
            EvChargeAgreementRequiredCodeRule("ev charge agreement needs an entry"),
            EvChargeAgreementVisibleGuardRule(3600),
            EvChargeAgreementTitleLengthRule(64),
            EvChargeAgreementTrailerIdLengthRule(32),
            EvChargeAgreementScoreRangeRule(2.0, 4225.4),
            EvChargeAgreementBatchSizeRangeRule(80, 255),
        ),
    )
}
