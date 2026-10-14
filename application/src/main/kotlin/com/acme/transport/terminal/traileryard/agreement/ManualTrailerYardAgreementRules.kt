package com.acme.transport.terminal.traileryard.agreement

/** A rule a manual trailer yard agreement must pass. Check returns its problems, or none. */
interface ManualTrailerYardAgreementRule {
    fun check(item: ManualTrailerYardAgreement): MutableList<String>
}

data class ManualTrailerYardAgreementNameLengthRule(var maxLength: Int) : ManualTrailerYardAgreementRule {
    override fun check(item: ManualTrailerYardAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardAgreementDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ManualTrailerYardAgreementRule {
    override fun check(item: ManualTrailerYardAgreement): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardAgreementRequiredNameRule(var message: String) : ManualTrailerYardAgreementRule {
    override fun check(item: ManualTrailerYardAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer yard agreement. */
data class ManualTrailerYardAgreementValidator(var rules: MutableList<ManualTrailerYardAgreementRule>) {
    fun validate(item: ManualTrailerYardAgreement): MutableList<String> {
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

    fun requireValid(item: ManualTrailerYardAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerYardAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerYardAgreementValidator(): ManualTrailerYardAgreementValidator {
    return ManualTrailerYardAgreementValidator(
        mutableListOf<ManualTrailerYardAgreementRule>(
            ManualTrailerYardAgreementNameLengthRule(255),
            ManualTrailerYardAgreementDurationMinutesRangeRule(120, 86400),
            ManualTrailerYardAgreementRequiredNameRule("manual trailer yard agreement needs this field"),
        ),
    )
}
