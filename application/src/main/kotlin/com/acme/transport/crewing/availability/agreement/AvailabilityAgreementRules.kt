package com.acme.transport.crewing.availability.agreement

/** A rule a availability agreement must pass. Check returns its problems, or none. */
interface AvailabilityAgreementRule {
    fun check(item: AvailabilityAgreement): MutableList<String>
}

data class AvailabilityAgreementRequiredLaneRule(var message: String) : AvailabilityAgreementRule {
    override fun check(item: AvailabilityAgreement): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAgreementOdometerRangeRule(var min: Int, var max: Int) : AvailabilityAgreementRule {
    override fun check(item: AvailabilityAgreement): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAgreementDockLengthRule(var maxLength: Int) : AvailabilityAgreementRule {
    override fun check(item: AvailabilityAgreement): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAgreementRequiredCodeRule(var message: String) : AvailabilityAgreementRule {
    override fun check(item: AvailabilityAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAgreementRequiredLabelRule(var message: String) : AvailabilityAgreementRule {
    override fun check(item: AvailabilityAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability agreement. */
data class AvailabilityAgreementValidator(var rules: MutableList<AvailabilityAgreementRule>) {
    fun validate(item: AvailabilityAgreement): MutableList<String> {
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

    fun requireValid(item: AvailabilityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityAgreementValidator(): AvailabilityAgreementValidator {
    return AvailabilityAgreementValidator(
        mutableListOf<AvailabilityAgreementRule>(
            AvailabilityAgreementRequiredLaneRule("availability agreement needs an entry"),
            AvailabilityAgreementOdometerRangeRule(16, 86400),
            AvailabilityAgreementDockLengthRule(3600),
            AvailabilityAgreementRequiredCodeRule("availability agreement needs an entry"),
            AvailabilityAgreementRequiredLabelRule("availability agreement needs a value"),
        ),
    )
}
