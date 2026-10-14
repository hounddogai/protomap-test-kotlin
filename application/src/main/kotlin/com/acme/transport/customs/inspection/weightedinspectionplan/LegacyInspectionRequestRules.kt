package com.acme.transport.customs.inspection.weightedinspectionplan

/** A rule a legacy inspection request must pass. Check returns its problems, or none. */
interface LegacyInspectionRequestRule {
    fun check(item: LegacyInspectionRequest): MutableList<String>
}

data class LegacyInspectionRequestCodePrefixRule(var prefix: String) : LegacyInspectionRequestRule {
    override fun check(item: LegacyInspectionRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionRequestEndsAtRangeRule(var min: Int, var max: Int) : LegacyInspectionRequestRule {
    override fun check(item: LegacyInspectionRequest): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionRequestRequiredOwnerIdRule(var message: String) : LegacyInspectionRequestRule {
    override fun check(item: LegacyInspectionRequest): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection request. */
data class LegacyInspectionRequestValidator(var rules: MutableList<LegacyInspectionRequestRule>) {
    fun validate(item: LegacyInspectionRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyInspectionRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionRequestValidator(): LegacyInspectionRequestValidator {
    return LegacyInspectionRequestValidator(
        mutableListOf<LegacyInspectionRequestRule>(
            LegacyInspectionRequestCodePrefixRule("OPS"),
            LegacyInspectionRequestEndsAtRangeRule(1000, 3600),
            LegacyInspectionRequestRequiredOwnerIdRule("legacy inspection request needs this field"),
        ),
    )
}
