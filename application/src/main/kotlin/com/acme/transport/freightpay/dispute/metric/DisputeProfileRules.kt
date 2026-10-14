package com.acme.transport.freightpay.dispute.metric

/** A rule a dispute profile must pass. Check returns its problems, or none. */
interface DisputeProfileRule {
    fun check(item: DisputeProfile): MutableList<String>
}

data class DisputeProfileRatioRangeRule(var min: Double, var max: Double) : DisputeProfileRule {
    override fun check(item: DisputeProfile): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class DisputeProfilePalletsRangeRule(var min: Int, var max: Int) : DisputeProfileRule {
    override fun check(item: DisputeProfile): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeProfileRequiredTitleRule(var message: String) : DisputeProfileRule {
    override fun check(item: DisputeProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeProfileCodeLengthRule(var maxLength: Int) : DisputeProfileRule {
    override fun check(item: DisputeProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeProfileFloorValueRangeRule(var min: Double, var max: Double) : DisputeProfileRule {
    override fun check(item: DisputeProfile): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute profile. */
data class DisputeProfileValidator(var rules: MutableList<DisputeProfileRule>) {
    fun validate(item: DisputeProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeProfileValidator(): DisputeProfileValidator {
    return DisputeProfileValidator(
        mutableListOf<DisputeProfileRule>(
            DisputeProfileRatioRangeRule(3.4, 3733.0),
            DisputeProfilePalletsRangeRule(16, 1000),
            DisputeProfileRequiredTitleRule("dispute profile needs a value"),
            DisputeProfileCodeLengthRule(64),
            DisputeProfileFloorValueRangeRule(4.3, 3529.1),
        ),
    )
}
