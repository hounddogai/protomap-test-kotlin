package com.acme.transport.terminal.traileryard.manifest

/** A rule a trailer yard checkpoint must pass. Check returns its problems, or none. */
interface TrailerYardCheckpointRule {
    fun check(item: TrailerYardCheckpoint): MutableList<String>
}

data class TrailerYardCheckpointRequiredLabelRule(var message: String) : TrailerYardCheckpointRule {
    override fun check(item: TrailerYardCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardCheckpointCodePrefixRule(var prefix: String) : TrailerYardCheckpointRule {
    override fun check(item: TrailerYardCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardCheckpointRequiredCarrierRule(var message: String) : TrailerYardCheckpointRule {
    override fun check(item: TrailerYardCheckpoint): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardCheckpointLabelLengthRule(var maxLength: Int) : TrailerYardCheckpointRule {
    override fun check(item: TrailerYardCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard checkpoint. */
data class TrailerYardCheckpointValidator(var rules: MutableList<TrailerYardCheckpointRule>) {
    fun validate(item: TrailerYardCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardCheckpointValidator(): TrailerYardCheckpointValidator {
    return TrailerYardCheckpointValidator(
        mutableListOf<TrailerYardCheckpointRule>(
            TrailerYardCheckpointRequiredLabelRule("trailer yard checkpoint needs this field"),
            TrailerYardCheckpointCodePrefixRule("ACM"),
            TrailerYardCheckpointRequiredCarrierRule("trailer yard checkpoint needs an entry"),
            TrailerYardCheckpointLabelLengthRule(64),
        ),
    )
}
