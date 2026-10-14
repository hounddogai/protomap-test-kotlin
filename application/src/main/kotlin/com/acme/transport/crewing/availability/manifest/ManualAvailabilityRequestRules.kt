package com.acme.transport.crewing.availability.manifest

/** A rule a manual availability request must pass. Check returns its problems, or none. */
interface ManualAvailabilityRequestRule {
    fun check(item: ManualAvailabilityRequest): MutableList<String>
}

data class ManualAvailabilityRequestTitleLengthRule(var maxLength: Int) : ManualAvailabilityRequestRule {
    override fun check(item: ManualAvailabilityRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilityRequestMarginRangeRule(
    var min: Double,
    var max: Double,
) : ManualAvailabilityRequestRule {
    override fun check(item: ManualAvailabilityRequest): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilityRequestRequiredCodeRule(var message: String) : ManualAvailabilityRequestRule {
    override fun check(item: ManualAvailabilityRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilityRequestRateRangeRule(
    var min: Double,
    var max: Double,
) : ManualAvailabilityRequestRule {
    override fun check(item: ManualAvailabilityRequest): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual availability request. */
data class ManualAvailabilityRequestValidator(var rules: MutableList<ManualAvailabilityRequestRule>) {
    fun validate(item: ManualAvailabilityRequest): MutableList<String> {
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

    fun requireValid(item: ManualAvailabilityRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAvailabilityRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAvailabilityRequestValidator(): ManualAvailabilityRequestValidator {
    return ManualAvailabilityRequestValidator(
        mutableListOf<ManualAvailabilityRequestRule>(
            ManualAvailabilityRequestTitleLengthRule(80),
            ManualAvailabilityRequestMarginRangeRule(6.4, 4988.5),
            ManualAvailabilityRequestRequiredCodeRule("manual availability request needs this field"),
            ManualAvailabilityRequestRateRangeRule(9.6, 3259.3),
        ),
    )
}
