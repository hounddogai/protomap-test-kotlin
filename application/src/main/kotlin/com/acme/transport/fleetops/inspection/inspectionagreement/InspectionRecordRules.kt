package com.acme.transport.fleetops.inspection.inspectionagreement

/** A rule a inspection record must pass. Check returns its problems, or none. */
interface InspectionRecordRule {
    fun check(item: InspectionRecord): MutableList<String>
}

data class InspectionRecordRequiredNameRule(var message: String) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRecordMaximumRangeRule(var min: Double, var max: Double) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class InspectionRecordRequiredDescriptionRule(var message: String) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRecordCodePrefixRule(var prefix: String) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRecordNameLengthRule(var maxLength: Int) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRecordRetriesRangeRule(var min: Int, var max: Int) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a inspection record. */
data class InspectionRecordValidator(var rules: MutableList<InspectionRecordRule>) {
    fun validate(item: InspectionRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionRecordValidator(): InspectionRecordValidator {
    return InspectionRecordValidator(
        mutableListOf<InspectionRecordRule>(
            InspectionRecordRequiredNameRule("inspection record needs an entry"),
            InspectionRecordMaximumRangeRule(1.4, 2078.5),
            InspectionRecordRequiredDescriptionRule("inspection record needs a value"),
            InspectionRecordCodePrefixRule("OPS"),
            InspectionRecordNameLengthRule(255),
            InspectionRecordRetriesRangeRule(32, 255),
        ),
    )
}
