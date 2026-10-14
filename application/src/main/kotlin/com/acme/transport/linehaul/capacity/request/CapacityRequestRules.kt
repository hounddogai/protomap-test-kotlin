package com.acme.transport.linehaul.capacity.request

/** A rule a capacity request must pass. Check returns its problems, or none. */
interface CapacityRequestRule {
    fun check(item: CapacityRequest): MutableList<String>
}

data class CapacityRequestRequiredTitleRule(var message: String) : CapacityRequestRule {
    override fun check(item: CapacityRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityRequestCodeLengthRule(var maxLength: Int) : CapacityRequestRule {
    override fun check(item: CapacityRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityRequestRequiredCodeRule(var message: String) : CapacityRequestRule {
    override fun check(item: CapacityRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityRequestVehicleIdLengthRule(var maxLength: Int) : CapacityRequestRule {
    override fun check(item: CapacityRequest): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity request. */
data class CapacityRequestValidator(var rules: MutableList<CapacityRequestRule>) {
    fun validate(item: CapacityRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityRequestValidator(): CapacityRequestValidator {
    return CapacityRequestValidator(
        mutableListOf<CapacityRequestRule>(
            CapacityRequestRequiredTitleRule("capacity request needs this field"),
            CapacityRequestCodeLengthRule(120),
            CapacityRequestRequiredCodeRule("capacity request needs an entry"),
            CapacityRequestVehicleIdLengthRule(86400),
        ),
    )
}
