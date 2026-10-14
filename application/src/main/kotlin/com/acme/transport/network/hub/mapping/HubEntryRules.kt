package com.acme.transport.network.hub.mapping

/** A rule a hub entry must pass. Check returns its problems, or none. */
interface HubEntryRule {
    fun check(item: HubEntry): MutableList<String>
}

data class HubEntryEndsAtRangeRule(var min: Int, var max: Int) : HubEntryRule {
    override fun check(item: HubEntry): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class HubEntryNameLengthRule(var maxLength: Int) : HubEntryRule {
    override fun check(item: HubEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubEntryRetriesRangeRule(var min: Int, var max: Int) : HubEntryRule {
    override fun check(item: HubEntry): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class HubEntryEtaMinutesRangeRule(var min: Int, var max: Int) : HubEntryRule {
    override fun check(item: HubEntry): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a hub entry. */
data class HubEntryValidator(var rules: MutableList<HubEntryRule>) {
    fun validate(item: HubEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultHubEntryValidator(): HubEntryValidator {
    return HubEntryValidator(
        mutableListOf<HubEntryRule>(
            HubEntryEndsAtRangeRule(64, 80),
            HubEntryNameLengthRule(120),
            HubEntryRetriesRangeRule(16, 32),
            HubEntryEtaMinutesRangeRule(255, 255),
        ),
    )
}
