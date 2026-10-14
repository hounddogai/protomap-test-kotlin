package com.acme.transport.freightpay.dispute.disputegroup

/** A rule a primary dispute checkpoint must pass. Check returns its problems, or none. */
interface PrimaryDisputeCheckpointRule {
    fun check(item: PrimaryDisputeCheckpoint): MutableList<String>
}

data class PrimaryDisputeCheckpointTrailerIdLengthRule(var maxLength: Int) : PrimaryDisputeCheckpointRule {
    override fun check(item: PrimaryDisputeCheckpoint): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDisputeCheckpointDockLengthRule(var maxLength: Int) : PrimaryDisputeCheckpointRule {
    override fun check(item: PrimaryDisputeCheckpoint): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDisputeCheckpointCodeLengthRule(var maxLength: Int) : PrimaryDisputeCheckpointRule {
    override fun check(item: PrimaryDisputeCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDisputeCheckpointDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryDisputeCheckpointRule {
    override fun check(item: PrimaryDisputeCheckpoint): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary dispute checkpoint. */
data class PrimaryDisputeCheckpointValidator(var rules: MutableList<PrimaryDisputeCheckpointRule>) {
    fun validate(item: PrimaryDisputeCheckpoint): MutableList<String> {
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

    fun requireValid(item: PrimaryDisputeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDisputeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDisputeCheckpointValidator(): PrimaryDisputeCheckpointValidator {
    return PrimaryDisputeCheckpointValidator(
        mutableListOf<PrimaryDisputeCheckpointRule>(
            PrimaryDisputeCheckpointTrailerIdLengthRule(255),
            PrimaryDisputeCheckpointDockLengthRule(120),
            PrimaryDisputeCheckpointCodeLengthRule(120),
            PrimaryDisputeCheckpointDistanceKmRangeRule(1.3, 361.9),
        ),
    )
}
