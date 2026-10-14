package com.acme.transport.customs.inspection.batch

/** A rule a inspection request must pass. Check returns its problems, or none. */
interface InspectionRequestRule {
    fun check(item: InspectionRequest): MutableList<String>
}

data class InspectionRequestTitleLengthRule(var maxLength: Int) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRequestCarrierLengthRule(var maxLength: Int) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRequestRequiredDescriptionRule(var message: String) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRequestDescriptionLengthRule(var maxLength: Int) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRequestPriorityRangeRule(var min: Int, var max: Int) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class InspectionRequestRequiredCodeRule(var message: String) : InspectionRequestRule {
    override fun check(item: InspectionRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
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
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
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
            InspectionRequestTitleLengthRule(3600),
            InspectionRequestCarrierLengthRule(16),
            InspectionRequestRequiredDescriptionRule("inspection request needs this field"),
            InspectionRequestDescriptionLengthRule(64),
            InspectionRequestPriorityRangeRule(1000, 3600),
            InspectionRequestRequiredCodeRule("inspection request needs a value"),
        ),
    )
}
