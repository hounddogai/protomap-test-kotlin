package com.acme.transport.customs.inspection.weightedinspectionplan

/** A rule a pending inspection checkpoint must pass. Check returns its problems, or none. */
interface PendingInspectionCheckpointRule {
    fun check(item: PendingInspectionCheckpoint): MutableList<String>
}

data class PendingInspectionCheckpointCurrencyLengthRule(var maxLength: Int) : PendingInspectionCheckpointRule {
    override fun check(item: PendingInspectionCheckpoint): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionCheckpointExternalRefLengthRule(
    var maxLength: Int,
) : PendingInspectionCheckpointRule {
    override fun check(item: PendingInspectionCheckpoint): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionCheckpointRequiredCurrencyRule(
    var message: String,
) : PendingInspectionCheckpointRule {
    override fun check(item: PendingInspectionCheckpoint): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionCheckpointDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : PendingInspectionCheckpointRule {
    override fun check(item: PendingInspectionCheckpoint): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionCheckpointNameLengthRule(var maxLength: Int) : PendingInspectionCheckpointRule {
    override fun check(item: PendingInspectionCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection checkpoint. */
data class PendingInspectionCheckpointValidator(var rules: MutableList<PendingInspectionCheckpointRule>) {
    fun validate(item: PendingInspectionCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingInspectionCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionCheckpointValidator(): PendingInspectionCheckpointValidator {
    return PendingInspectionCheckpointValidator(
        mutableListOf<PendingInspectionCheckpointRule>(
            PendingInspectionCheckpointCurrencyLengthRule(80),
            PendingInspectionCheckpointExternalRefLengthRule(3600),
            PendingInspectionCheckpointRequiredCurrencyRule("pending inspection checkpoint needs an entry"),
            PendingInspectionCheckpointDistanceKmRangeRule(2.9, 209.2),
            PendingInspectionCheckpointNameLengthRule(32),
        ),
    )
}
