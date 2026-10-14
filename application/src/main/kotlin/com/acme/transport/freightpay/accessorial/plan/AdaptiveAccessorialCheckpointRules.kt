package com.acme.transport.freightpay.accessorial.plan

/** A rule a adaptive accessorial checkpoint must pass. Check returns its problems, or none. */
interface AdaptiveAccessorialCheckpointRule {
    fun check(item: AdaptiveAccessorialCheckpoint): MutableList<String>
}

data class AdaptiveAccessorialCheckpointRequiredDriverIdRule(
    var message: String,
) : AdaptiveAccessorialCheckpointRule {
    override fun check(item: AdaptiveAccessorialCheckpoint): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialCheckpointCodeLengthRule(var maxLength: Int) : AdaptiveAccessorialCheckpointRule {
    override fun check(item: AdaptiveAccessorialCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialCheckpointRequiredContactEmailRule(
    var message: String,
) : AdaptiveAccessorialCheckpointRule {
    override fun check(item: AdaptiveAccessorialCheckpoint): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialCheckpointRequiredCodeRule(
    var message: String,
) : AdaptiveAccessorialCheckpointRule {
    override fun check(item: AdaptiveAccessorialCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialCheckpointContactEmailLengthRule(
    var maxLength: Int,
) : AdaptiveAccessorialCheckpointRule {
    override fun check(item: AdaptiveAccessorialCheckpoint): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive accessorial checkpoint. */
data class AdaptiveAccessorialCheckpointValidator(var rules: MutableList<AdaptiveAccessorialCheckpointRule>) {
    fun validate(item: AdaptiveAccessorialCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAccessorialCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAccessorialCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAccessorialCheckpointValidator(): AdaptiveAccessorialCheckpointValidator {
    return AdaptiveAccessorialCheckpointValidator(
        mutableListOf<AdaptiveAccessorialCheckpointRule>(
            AdaptiveAccessorialCheckpointRequiredDriverIdRule("adaptive accessorial checkpoint needs an entry"),
            AdaptiveAccessorialCheckpointCodeLengthRule(64),
            AdaptiveAccessorialCheckpointRequiredContactEmailRule(
                "adaptive accessorial checkpoint needs this field",
            ),
            AdaptiveAccessorialCheckpointRequiredCodeRule("adaptive accessorial checkpoint needs this field"),
            AdaptiveAccessorialCheckpointContactEmailLengthRule(86400),
        ),
    )
}
