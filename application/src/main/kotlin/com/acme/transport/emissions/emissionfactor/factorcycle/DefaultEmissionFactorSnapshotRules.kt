package com.acme.transport.emissions.emissionfactor.factorcycle

/** A rule a default emission factor snapshot must pass. Check returns its problems, or none. */
interface DefaultEmissionFactorSnapshotRule {
    fun check(item: DefaultEmissionFactorSnapshot): MutableList<String>
}

data class DefaultEmissionFactorSnapshotRequiredCodeRule(
    var message: String,
) : DefaultEmissionFactorSnapshotRule {
    override fun check(item: DefaultEmissionFactorSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorSnapshotUnitLengthRule(var maxLength: Int) : DefaultEmissionFactorSnapshotRule {
    override fun check(item: DefaultEmissionFactorSnapshot): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorSnapshotTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DefaultEmissionFactorSnapshotRule {
    override fun check(item: DefaultEmissionFactorSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class DefaultEmissionFactorSnapshotCodeLengthRule(var maxLength: Int) : DefaultEmissionFactorSnapshotRule {
    override fun check(item: DefaultEmissionFactorSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorSnapshotEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : DefaultEmissionFactorSnapshotRule {
    override fun check(item: DefaultEmissionFactorSnapshot): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default emission factor snapshot. */
data class DefaultEmissionFactorSnapshotValidator(var rules: MutableList<DefaultEmissionFactorSnapshotRule>) {
    fun validate(item: DefaultEmissionFactorSnapshot): MutableList<String> {
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

    fun requireValid(item: DefaultEmissionFactorSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEmissionFactorSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEmissionFactorSnapshotValidator(): DefaultEmissionFactorSnapshotValidator {
    return DefaultEmissionFactorSnapshotValidator(
        mutableListOf<DefaultEmissionFactorSnapshotRule>(
            DefaultEmissionFactorSnapshotRequiredCodeRule("default emission factor snapshot needs an entry"),
            DefaultEmissionFactorSnapshotUnitLengthRule(64),
            DefaultEmissionFactorSnapshotTagsLimitRule(80, mutableListOf<String>()),
            DefaultEmissionFactorSnapshotCodeLengthRule(80),
            DefaultEmissionFactorSnapshotEtaMinutesRangeRule(32, 64),
        ),
    )
}
