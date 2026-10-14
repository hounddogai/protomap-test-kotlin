package com.acme.transport.customs.clearance.milestone

/** A rule a manual clearance manifest must pass. Check returns its problems, or none. */
interface ManualClearanceManifestRule {
    fun check(item: ManualClearanceManifest): MutableList<String>
}

data class ManualClearanceManifestShiftCodeLengthRule(var maxLength: Int) : ManualClearanceManifestRule {
    override fun check(item: ManualClearanceManifest): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceManifestRequiredCodeRule(var message: String) : ManualClearanceManifestRule {
    override fun check(item: ManualClearanceManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceManifestRatioRangeRule(
    var min: Double,
    var max: Double,
) : ManualClearanceManifestRule {
    override fun check(item: ManualClearanceManifest): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual clearance manifest. */
data class ManualClearanceManifestValidator(var rules: MutableList<ManualClearanceManifestRule>) {
    fun validate(item: ManualClearanceManifest): MutableList<String> {
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

    fun requireValid(item: ManualClearanceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualClearanceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualClearanceManifestValidator(): ManualClearanceManifestValidator {
    return ManualClearanceManifestValidator(
        mutableListOf<ManualClearanceManifestRule>(
            ManualClearanceManifestShiftCodeLengthRule(1000),
            ManualClearanceManifestRequiredCodeRule("manual clearance manifest needs an entry"),
            ManualClearanceManifestRatioRangeRule(9.8, 1067.6),
        ),
    )
}
