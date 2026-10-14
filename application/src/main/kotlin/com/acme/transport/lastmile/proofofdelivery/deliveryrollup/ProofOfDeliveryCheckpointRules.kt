package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

/** A rule a proof of delivery checkpoint must pass. Check returns its problems, or none. */
interface ProofOfDeliveryCheckpointRule {
    fun check(item: ProofOfDeliveryCheckpoint): MutableList<String>
}

data class ProofOfDeliveryCheckpointRequiredTitleRule(var message: String) : ProofOfDeliveryCheckpointRule {
    override fun check(item: ProofOfDeliveryCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryCheckpointRouteCodeLengthRule(var maxLength: Int) : ProofOfDeliveryCheckpointRule {
    override fun check(item: ProofOfDeliveryCheckpoint): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryCheckpointPriorityRangeRule(
    var min: Int,
    var max: Int,
) : ProofOfDeliveryCheckpointRule {
    override fun check(item: ProofOfDeliveryCheckpoint): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryCheckpointTitleLengthRule(var maxLength: Int) : ProofOfDeliveryCheckpointRule {
    override fun check(item: ProofOfDeliveryCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryCheckpointCodeLengthRule(var maxLength: Int) : ProofOfDeliveryCheckpointRule {
    override fun check(item: ProofOfDeliveryCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryCheckpointExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ProofOfDeliveryCheckpointRule {
    override fun check(item: ProofOfDeliveryCheckpoint): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery checkpoint. */
data class ProofOfDeliveryCheckpointValidator(var rules: MutableList<ProofOfDeliveryCheckpointRule>) {
    fun validate(item: ProofOfDeliveryCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryCheckpointValidator(): ProofOfDeliveryCheckpointValidator {
    return ProofOfDeliveryCheckpointValidator(
        mutableListOf<ProofOfDeliveryCheckpointRule>(
            ProofOfDeliveryCheckpointRequiredTitleRule("proof of delivery checkpoint needs a value"),
            ProofOfDeliveryCheckpointRouteCodeLengthRule(3600),
            ProofOfDeliveryCheckpointPriorityRangeRule(64, 1000),
            ProofOfDeliveryCheckpointTitleLengthRule(1000),
            ProofOfDeliveryCheckpointCodeLengthRule(32),
            ProofOfDeliveryCheckpointExpiresAtRangeRule(80, 80),
        ),
    )
}
