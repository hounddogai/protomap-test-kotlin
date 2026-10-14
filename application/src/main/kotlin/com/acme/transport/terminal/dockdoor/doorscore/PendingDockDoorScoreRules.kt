package com.acme.transport.terminal.dockdoor.doorscore

/** A rule a pending dock door score must pass. Check returns its problems, or none. */
interface PendingDockDoorScoreRule {
    fun check(item: PendingDockDoorScore): MutableList<String>
}

data class PendingDockDoorScoreTitleLengthRule(var maxLength: Int) : PendingDockDoorScoreRule {
    override fun check(item: PendingDockDoorScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorScorePalletsRangeRule(var min: Int, var max: Int) : PendingDockDoorScoreRule {
    override fun check(item: PendingDockDoorScore): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class PendingDockDoorScoreRequiredTrailerIdRule(var message: String) : PendingDockDoorScoreRule {
    override fun check(item: PendingDockDoorScore): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorScoreCodeLengthRule(var maxLength: Int) : PendingDockDoorScoreRule {
    override fun check(item: PendingDockDoorScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dock door score. */
data class PendingDockDoorScoreValidator(var rules: MutableList<PendingDockDoorScoreRule>) {
    fun validate(item: PendingDockDoorScore): MutableList<String> {
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

    fun requireValid(item: PendingDockDoorScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDockDoorScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDockDoorScoreValidator(): PendingDockDoorScoreValidator {
    return PendingDockDoorScoreValidator(
        mutableListOf<PendingDockDoorScoreRule>(
            PendingDockDoorScoreTitleLengthRule(1000),
            PendingDockDoorScorePalletsRangeRule(16, 1000),
            PendingDockDoorScoreRequiredTrailerIdRule("pending dock door score needs a value"),
            PendingDockDoorScoreCodeLengthRule(16),
        ),
    )
}
