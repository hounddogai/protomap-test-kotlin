package com.acme.transport.network.servicearea.areaclaim

/** A rule a default service area snapshot must pass. Check returns its problems, or none. */
interface DefaultServiceAreaSnapshotRule {
    fun check(item: DefaultServiceAreaSnapshot): MutableList<String>
}

data class DefaultServiceAreaSnapshotRequiredNameRule(var message: String) : DefaultServiceAreaSnapshotRule {
    override fun check(item: DefaultServiceAreaSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaSnapshotSequenceRangeRule(
    var min: Int,
    var max: Int,
) : DefaultServiceAreaSnapshotRule {
    override fun check(item: DefaultServiceAreaSnapshot): MutableList<String> {
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

data class DefaultServiceAreaSnapshotDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : DefaultServiceAreaSnapshotRule {
    override fun check(item: DefaultServiceAreaSnapshot): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaSnapshotRequiredCodeRule(var message: String) : DefaultServiceAreaSnapshotRule {
    override fun check(item: DefaultServiceAreaSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default service area snapshot. */
data class DefaultServiceAreaSnapshotValidator(var rules: MutableList<DefaultServiceAreaSnapshotRule>) {
    fun validate(item: DefaultServiceAreaSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultServiceAreaSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultServiceAreaSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultServiceAreaSnapshotValidator(): DefaultServiceAreaSnapshotValidator {
    return DefaultServiceAreaSnapshotValidator(
        mutableListOf<DefaultServiceAreaSnapshotRule>(
            DefaultServiceAreaSnapshotRequiredNameRule("default service area snapshot needs an entry"),
            DefaultServiceAreaSnapshotSequenceRangeRule(1000, 1000),
            DefaultServiceAreaSnapshotDistanceKmRangeRule(6.9, 4204.9),
            DefaultServiceAreaSnapshotRequiredCodeRule("default service area snapshot needs a value"),
        ),
    )
}
