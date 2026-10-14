package com.acme.transport.linehaul.tender.estimate

/** A rule a tender snapshot must pass. Check returns its problems, or none. */
interface TenderSnapshotRule {
    fun check(item: TenderSnapshot): MutableList<String>
}

data class TenderSnapshotLabelLengthRule(var maxLength: Int) : TenderSnapshotRule {
    override fun check(item: TenderSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderSnapshotUnitLengthRule(var maxLength: Int) : TenderSnapshotRule {
    override fun check(item: TenderSnapshot): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderSnapshotGraceDaysRangeRule(var min: Int, var max: Int) : TenderSnapshotRule {
    override fun check(item: TenderSnapshot): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender snapshot. */
data class TenderSnapshotValidator(var rules: MutableList<TenderSnapshotRule>) {
    fun validate(item: TenderSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderSnapshotValidator(): TenderSnapshotValidator {
    return TenderSnapshotValidator(
        mutableListOf<TenderSnapshotRule>(
            TenderSnapshotLabelLengthRule(255),
            TenderSnapshotUnitLengthRule(86400),
            TenderSnapshotGraceDaysRangeRule(80, 86400),
        ),
    )
}
