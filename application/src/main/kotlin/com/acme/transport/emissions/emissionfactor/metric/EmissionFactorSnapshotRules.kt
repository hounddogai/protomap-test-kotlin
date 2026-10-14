package com.acme.transport.emissions.emissionfactor.metric

/** A rule a emission factor snapshot must pass. Check returns its problems, or none. */
interface EmissionFactorSnapshotRule {
    fun check(item: EmissionFactorSnapshot): MutableList<String>
}

data class EmissionFactorSnapshotRequiredChannelRule(var message: String) : EmissionFactorSnapshotRule {
    override fun check(item: EmissionFactorSnapshot): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorSnapshotLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : EmissionFactorSnapshotRule {
    override fun check(item: EmissionFactorSnapshot): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorSnapshotGraceDaysRangeRule(var min: Int, var max: Int) : EmissionFactorSnapshotRule {
    override fun check(item: EmissionFactorSnapshot): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a emission factor snapshot. */
data class EmissionFactorSnapshotValidator(var rules: MutableList<EmissionFactorSnapshotRule>) {
    fun validate(item: EmissionFactorSnapshot): MutableList<String> {
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

    fun requireValid(item: EmissionFactorSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorSnapshotValidator(): EmissionFactorSnapshotValidator {
    return EmissionFactorSnapshotValidator(
        mutableListOf<EmissionFactorSnapshotRule>(
            EmissionFactorSnapshotRequiredChannelRule("emission factor snapshot needs a value"),
            EmissionFactorSnapshotLoadKgRangeRule(7.0, 4249.5),
            EmissionFactorSnapshotGraceDaysRangeRule(32, 64),
        ),
    )
}
