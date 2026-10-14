package com.acme.transport.fleetops.defect.defectforecast

/** A rule a legacy defect snapshot must pass. Check returns its problems, or none. */
interface LegacyDefectSnapshotRule {
    fun check(item: LegacyDefectSnapshot): MutableList<String>
}

data class LegacyDefectSnapshotSequenceRangeRule(var min: Int, var max: Int) : LegacyDefectSnapshotRule {
    override fun check(item: LegacyDefectSnapshot): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class LegacyDefectSnapshotRatioRangeRule(var min: Double, var max: Double) : LegacyDefectSnapshotRule {
    override fun check(item: LegacyDefectSnapshot): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectSnapshotNameLengthRule(var maxLength: Int) : LegacyDefectSnapshotRule {
    override fun check(item: LegacyDefectSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectSnapshotRequiredCodeRule(var message: String) : LegacyDefectSnapshotRule {
    override fun check(item: LegacyDefectSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectSnapshotCodeLengthRule(var maxLength: Int) : LegacyDefectSnapshotRule {
    override fun check(item: LegacyDefectSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectSnapshotRequiredNameRule(var message: String) : LegacyDefectSnapshotRule {
    override fun check(item: LegacyDefectSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy defect snapshot. */
data class LegacyDefectSnapshotValidator(var rules: MutableList<LegacyDefectSnapshotRule>) {
    fun validate(item: LegacyDefectSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDefectSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDefectSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDefectSnapshotValidator(): LegacyDefectSnapshotValidator {
    return LegacyDefectSnapshotValidator(
        mutableListOf<LegacyDefectSnapshotRule>(
            LegacyDefectSnapshotSequenceRangeRule(64, 1000),
            LegacyDefectSnapshotRatioRangeRule(3.2, 3011.1),
            LegacyDefectSnapshotNameLengthRule(16),
            LegacyDefectSnapshotRequiredCodeRule("legacy defect snapshot needs an entry"),
            LegacyDefectSnapshotCodeLengthRule(255),
            LegacyDefectSnapshotRequiredNameRule("legacy defect snapshot needs this field"),
        ),
    )
}
