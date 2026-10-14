package com.acme.transport.fleetops.inspection.mapping

/** A rule a inspection request must pass. Check returns its problems, or none. */
interface InspectionRequestRule {
    fun check(item: InspectionRequest): MutableList<String>
}

data class InspectionRequestCodeLengthRule(var maxLength: Int) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRequestRequiredRouteCodeRule(var message: String) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRequestAttemptsRangeRule(var min: Int, var max: Int) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRequestStartsAtRangeRule(var min: Int, var max: Int) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class InspectionRequestRouteCodeLengthRule(var maxLength: Int) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection request. */
data class InspectionRequestValidator(var rules: MutableList<InspectionRequestRule>) {
    fun validate(item: InspectionRequest): MutableList<String> {
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

    fun requireValid(item: InspectionRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionRequestValidator(): InspectionRequestValidator {
    return InspectionRequestValidator(
        mutableListOf<InspectionRequestRule>(
            InspectionRequestCodeLengthRule(120),
            InspectionRequestRequiredRouteCodeRule("inspection request needs an entry"),
            InspectionRequestAttemptsRangeRule(64, 86400),
            InspectionRequestStartsAtRangeRule(32, 120),
            InspectionRequestRouteCodeLengthRule(80),
        ),
    )
}
