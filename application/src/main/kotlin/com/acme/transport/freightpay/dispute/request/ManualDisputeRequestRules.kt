package com.acme.transport.freightpay.dispute.request

/** A rule a manual dispute request must pass. Check returns its problems, or none. */
interface ManualDisputeRequestRule {
    fun check(item: ManualDisputeRequest): MutableList<String>
}

data class ManualDisputeRequestRequiredContactEmailRule(var message: String) : ManualDisputeRequestRule {
    override fun check(item: ManualDisputeRequest): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeRequestCodeLengthRule(var maxLength: Int) : ManualDisputeRequestRule {
    override fun check(item: ManualDisputeRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeRequestRequiredDriverIdRule(var message: String) : ManualDisputeRequestRule {
    override fun check(item: ManualDisputeRequest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeRequestDiscountRangeRule(var min: Double, var max: Double) : ManualDisputeRequestRule {
    override fun check(item: ManualDisputeRequest): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dispute request. */
data class ManualDisputeRequestValidator(var rules: MutableList<ManualDisputeRequestRule>) {
    fun validate(item: ManualDisputeRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDisputeRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDisputeRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDisputeRequestValidator(): ManualDisputeRequestValidator {
    return ManualDisputeRequestValidator(
        mutableListOf<ManualDisputeRequestRule>(
            ManualDisputeRequestRequiredContactEmailRule("manual dispute request needs an entry"),
            ManualDisputeRequestCodeLengthRule(64),
            ManualDisputeRequestRequiredDriverIdRule("manual dispute request needs this field"),
            ManualDisputeRequestDiscountRangeRule(0.2, 3648.2),
        ),
    )
}
