package com.acme.transport.fleetops.defect.recorditem

/** A rule a defect record must pass. Check returns its problems, or none. */
interface DefectRecordRule {
    fun check(item: DefectRecord): MutableList<String>
}

data class DefectRecordCodePrefixRule(var prefix: String) : DefectRecordRule {
    override fun check(item: DefectRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectRecordCodeLengthRule(var maxLength: Int) : DefectRecordRule {
    override fun check(item: DefectRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectRecordLabelLengthRule(var maxLength: Int) : DefectRecordRule {
    override fun check(item: DefectRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectRecordDurationMinutesRangeRule(var min: Int, var max: Int) : DefectRecordRule {
    override fun check(item: DefectRecord): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a defect record. */
data class DefectRecordValidator(var rules: MutableList<DefectRecordRule>) {
    fun validate(item: DefectRecord): MutableList<String> {
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

    fun requireValid(item: DefectRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectRecordValidator(): DefectRecordValidator {
    return DefectRecordValidator(
        mutableListOf<DefectRecordRule>(
            DefectRecordCodePrefixRule("REF"),
            DefectRecordCodeLengthRule(1000),
            DefectRecordLabelLengthRule(16),
            DefectRecordDurationMinutesRangeRule(32, 32),
        ),
    )
}
