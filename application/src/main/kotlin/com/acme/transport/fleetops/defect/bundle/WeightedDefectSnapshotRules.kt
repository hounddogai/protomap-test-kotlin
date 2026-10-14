package com.acme.transport.fleetops.defect.bundle

/** A rule a weighted defect snapshot must pass. Check returns its problems, or none. */
interface WeightedDefectSnapshotRule {
    fun check(item: WeightedDefectSnapshot): MutableList<String>
}

data class WeightedDefectSnapshotCountryLengthRule(var maxLength: Int) : WeightedDefectSnapshotRule {
    override fun check(item: WeightedDefectSnapshot): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectSnapshotCodeLengthRule(var maxLength: Int) : WeightedDefectSnapshotRule {
    override fun check(item: WeightedDefectSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectSnapshotRequiredLabelRule(var message: String) : WeightedDefectSnapshotRule {
    override fun check(item: WeightedDefectSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectSnapshotRequiredCodeRule(var message: String) : WeightedDefectSnapshotRule {
    override fun check(item: WeightedDefectSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectSnapshotRequiredChecksumRule(var message: String) : WeightedDefectSnapshotRule {
    override fun check(item: WeightedDefectSnapshot): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted defect snapshot. */
data class WeightedDefectSnapshotValidator(var rules: MutableList<WeightedDefectSnapshotRule>) {
    fun validate(item: WeightedDefectSnapshot): MutableList<String> {
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

    fun requireValid(item: WeightedDefectSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDefectSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDefectSnapshotValidator(): WeightedDefectSnapshotValidator {
    return WeightedDefectSnapshotValidator(
        mutableListOf<WeightedDefectSnapshotRule>(
            WeightedDefectSnapshotCountryLengthRule(255),
            WeightedDefectSnapshotCodeLengthRule(16),
            WeightedDefectSnapshotRequiredLabelRule("weighted defect snapshot needs this field"),
            WeightedDefectSnapshotRequiredCodeRule("weighted defect snapshot needs an entry"),
            WeightedDefectSnapshotRequiredChecksumRule("weighted defect snapshot needs an entry"),
        ),
    )
}
