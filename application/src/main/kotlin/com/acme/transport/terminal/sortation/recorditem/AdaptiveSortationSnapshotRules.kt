package com.acme.transport.terminal.sortation.recorditem

/** A rule a adaptive sortation snapshot must pass. Check returns its problems, or none. */
interface AdaptiveSortationSnapshotRule {
    fun check(item: AdaptiveSortationSnapshot): MutableList<String>
}

data class AdaptiveSortationSnapshotNameLengthRule(var maxLength: Int) : AdaptiveSortationSnapshotRule {
    override fun check(item: AdaptiveSortationSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationSnapshotVersionRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveSortationSnapshotRule {
    override fun check(item: AdaptiveSortationSnapshot): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationSnapshotContactEmailLengthRule(var maxLength: Int) : AdaptiveSortationSnapshotRule {
    override fun check(item: AdaptiveSortationSnapshot): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationSnapshotBudgetRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveSortationSnapshotRule {
    override fun check(item: AdaptiveSortationSnapshot): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class AdaptiveSortationSnapshotRequiredCodeRule(var message: String) : AdaptiveSortationSnapshotRule {
    override fun check(item: AdaptiveSortationSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive sortation snapshot. */
data class AdaptiveSortationSnapshotValidator(var rules: MutableList<AdaptiveSortationSnapshotRule>) {
    fun validate(item: AdaptiveSortationSnapshot): MutableList<String> {
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

    fun requireValid(item: AdaptiveSortationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSortationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSortationSnapshotValidator(): AdaptiveSortationSnapshotValidator {
    return AdaptiveSortationSnapshotValidator(
        mutableListOf<AdaptiveSortationSnapshotRule>(
            AdaptiveSortationSnapshotNameLengthRule(120),
            AdaptiveSortationSnapshotVersionRangeRule(32, 32),
            AdaptiveSortationSnapshotContactEmailLengthRule(3600),
            AdaptiveSortationSnapshotBudgetRangeRule(8.2, 4159.6),
            AdaptiveSortationSnapshotRequiredCodeRule("adaptive sortation snapshot needs an entry"),
        ),
    )
}
