package com.acme.transport.linehaul.trailer.threshold

/** A rule a trailer checkpoint must pass. Check returns its problems, or none. */
interface TrailerCheckpointRule {
    fun check(item: TrailerCheckpoint): MutableList<String>
}

data class TrailerCheckpointLabelLengthRule(var maxLength: Int) : TrailerCheckpointRule {
    override fun check(item: TrailerCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerCheckpointRequiredLabelRule(var message: String) : TrailerCheckpointRule {
    override fun check(item: TrailerCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerCheckpointRequiredChecksumRule(var message: String) : TrailerCheckpointRule {
    override fun check(item: TrailerCheckpoint): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerCheckpointCodeLengthRule(var maxLength: Int) : TrailerCheckpointRule {
    override fun check(item: TrailerCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerCheckpointChecksumLengthRule(var maxLength: Int) : TrailerCheckpointRule {
    override fun check(item: TrailerCheckpoint): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer checkpoint. */
data class TrailerCheckpointValidator(var rules: MutableList<TrailerCheckpointRule>) {
    fun validate(item: TrailerCheckpoint): MutableList<String> {
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

    fun requireValid(item: TrailerCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerCheckpointValidator(): TrailerCheckpointValidator {
    return TrailerCheckpointValidator(
        mutableListOf<TrailerCheckpointRule>(
            TrailerCheckpointLabelLengthRule(32),
            TrailerCheckpointRequiredLabelRule("trailer checkpoint needs an entry"),
            TrailerCheckpointRequiredChecksumRule("trailer checkpoint needs an entry"),
            TrailerCheckpointCodeLengthRule(16),
            TrailerCheckpointChecksumLengthRule(1000),
        ),
    )
}
