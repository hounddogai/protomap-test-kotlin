package com.acme.transport.lastmile.parcel.policy

/** A rule a parcel snapshot must pass. Check returns its problems, or none. */
interface ParcelSnapshotRule {
    fun check(item: ParcelSnapshot): MutableList<String>
}

data class ParcelSnapshotRequiredReviewerRule(var message: String) : ParcelSnapshotRule {
    override fun check(item: ParcelSnapshot): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelSnapshotRequiredCodeRule(var message: String) : ParcelSnapshotRule {
    override fun check(item: ParcelSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelSnapshotRequiredNameRule(var message: String) : ParcelSnapshotRule {
    override fun check(item: ParcelSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelSnapshotNameLengthRule(var maxLength: Int) : ParcelSnapshotRule {
    override fun check(item: ParcelSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelSnapshotLabelLengthRule(var maxLength: Int) : ParcelSnapshotRule {
    override fun check(item: ParcelSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelSnapshotCapacityRangeRule(var min: Int, var max: Int) : ParcelSnapshotRule {
    override fun check(item: ParcelSnapshot): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel snapshot. */
data class ParcelSnapshotValidator(var rules: MutableList<ParcelSnapshotRule>) {
    fun validate(item: ParcelSnapshot): MutableList<String> {
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

    fun requireValid(item: ParcelSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelSnapshotValidator(): ParcelSnapshotValidator {
    return ParcelSnapshotValidator(
        mutableListOf<ParcelSnapshotRule>(
            ParcelSnapshotRequiredReviewerRule("parcel snapshot needs a value"),
            ParcelSnapshotRequiredCodeRule("parcel snapshot needs this field"),
            ParcelSnapshotRequiredNameRule("parcel snapshot needs this field"),
            ParcelSnapshotNameLengthRule(1000),
            ParcelSnapshotLabelLengthRule(64),
            ParcelSnapshotCapacityRangeRule(64, 86400),
        ),
    )
}
