package com.acme.transport.customs.inspection.target

/** A rule a manual inspection record must pass. Check returns its problems, or none. */
interface ManualInspectionRecordRule {
    fun check(item: ManualInspectionRecord): MutableList<String>
}

data class ManualInspectionRecordRequiredRegionRule(var message: String) : ManualInspectionRecordRule {
    override fun check(item: ManualInspectionRecord): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionRecordRequiredNameRule(var message: String) : ManualInspectionRecordRule {
    override fun check(item: ManualInspectionRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionRecordVersionRangeRule(var min: Int, var max: Int) : ManualInspectionRecordRule {
    override fun check(item: ManualInspectionRecord): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual inspection record. */
data class ManualInspectionRecordValidator(var rules: MutableList<ManualInspectionRecordRule>) {
    fun validate(item: ManualInspectionRecord): MutableList<String> {
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

    fun requireValid(item: ManualInspectionRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualInspectionRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultManualInspectionRecordValidator(): ManualInspectionRecordValidator {
    return ManualInspectionRecordValidator(
        mutableListOf<ManualInspectionRecordRule>(
            ManualInspectionRecordRequiredRegionRule("manual inspection record needs an entry"),
            ManualInspectionRecordRequiredNameRule("manual inspection record needs this field"),
            ManualInspectionRecordVersionRangeRule(3600, 3600),
        ),
    )
}
