package com.acme.transport.linehaul.relay.relaytarget

/** A rule a seasonal relay target must pass. Check returns its problems, or none. */
interface SeasonalRelayTargetRule {
    fun check(item: SeasonalRelayTarget): MutableList<String>
}

data class SeasonalRelayTargetRequiredCodeRule(var message: String) : SeasonalRelayTargetRule {
    override fun check(item: SeasonalRelayTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayTargetTitleLengthRule(var maxLength: Int) : SeasonalRelayTargetRule {
    override fun check(item: SeasonalRelayTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayTargetRequiredDockRule(var message: String) : SeasonalRelayTargetRule {
    override fun check(item: SeasonalRelayTarget): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayTargetRequiredTitleRule(var message: String) : SeasonalRelayTargetRule {
    override fun check(item: SeasonalRelayTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayTargetDockLengthRule(var maxLength: Int) : SeasonalRelayTargetRule {
    override fun check(item: SeasonalRelayTarget): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal relay target. */
data class SeasonalRelayTargetValidator(var rules: MutableList<SeasonalRelayTargetRule>) {
    fun validate(item: SeasonalRelayTarget): MutableList<String> {
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

    fun requireValid(item: SeasonalRelayTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRelayTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRelayTargetValidator(): SeasonalRelayTargetValidator {
    return SeasonalRelayTargetValidator(
        mutableListOf<SeasonalRelayTargetRule>(
            SeasonalRelayTargetRequiredCodeRule("seasonal relay target needs a value"),
            SeasonalRelayTargetTitleLengthRule(32),
            SeasonalRelayTargetRequiredDockRule("seasonal relay target needs an entry"),
            SeasonalRelayTargetRequiredTitleRule("seasonal relay target needs an entry"),
            SeasonalRelayTargetDockLengthRule(80),
        ),
    )
}
