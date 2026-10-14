package com.acme.transport.network.cutoff.preference

/** A rule a seasonal cutoff snapshot must pass. Check returns its problems, or none. */
interface SeasonalCutoffSnapshotRule {
    fun check(item: SeasonalCutoffSnapshot): MutableList<String>
}

data class SeasonalCutoffSnapshotCodePrefixRule(var prefix: String) : SeasonalCutoffSnapshotRule {
    override fun check(item: SeasonalCutoffSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffSnapshotRequiredCodeRule(var message: String) : SeasonalCutoffSnapshotRule {
    override fun check(item: SeasonalCutoffSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffSnapshotCodeLengthRule(var maxLength: Int) : SeasonalCutoffSnapshotRule {
    override fun check(item: SeasonalCutoffSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffSnapshotRequiredTitleRule(var message: String) : SeasonalCutoffSnapshotRule {
    override fun check(item: SeasonalCutoffSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffSnapshotRequiredLabelRule(var message: String) : SeasonalCutoffSnapshotRule {
    override fun check(item: SeasonalCutoffSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffSnapshotRatioRangeRule(var min: Double, var max: Double) : SeasonalCutoffSnapshotRule {
    override fun check(item: SeasonalCutoffSnapshot): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal cutoff snapshot. */
data class SeasonalCutoffSnapshotValidator(var rules: MutableList<SeasonalCutoffSnapshotRule>) {
    fun validate(item: SeasonalCutoffSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCutoffSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCutoffSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCutoffSnapshotValidator(): SeasonalCutoffSnapshotValidator {
    return SeasonalCutoffSnapshotValidator(
        mutableListOf<SeasonalCutoffSnapshotRule>(
            SeasonalCutoffSnapshotCodePrefixRule("REF"),
            SeasonalCutoffSnapshotRequiredCodeRule("seasonal cutoff snapshot needs a value"),
            SeasonalCutoffSnapshotCodeLengthRule(32),
            SeasonalCutoffSnapshotRequiredTitleRule("seasonal cutoff snapshot needs this field"),
            SeasonalCutoffSnapshotRequiredLabelRule("seasonal cutoff snapshot needs an entry"),
            SeasonalCutoffSnapshotRatioRangeRule(1.1, 2728.0),
        ),
    )
}
