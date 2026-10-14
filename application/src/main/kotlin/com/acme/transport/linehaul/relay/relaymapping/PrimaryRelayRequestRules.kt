package com.acme.transport.linehaul.relay.relaymapping

/** A rule a primary relay request must pass. Check returns its problems, or none. */
interface PrimaryRelayRequestRule {
    fun check(item: PrimaryRelayRequest): MutableList<String>
}

data class PrimaryRelayRequestRequiredTitleRule(var message: String) : PrimaryRelayRequestRule {
    override fun check(item: PrimaryRelayRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRelayRequestWeightRangeRule(var min: Double, var max: Double) : PrimaryRelayRequestRule {
    override fun check(item: PrimaryRelayRequest): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRelayRequestRequiredChecksumRule(var message: String) : PrimaryRelayRequestRule {
    override fun check(item: PrimaryRelayRequest): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRelayRequestNameLengthRule(var maxLength: Int) : PrimaryRelayRequestRule {
    override fun check(item: PrimaryRelayRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary relay request. */
data class PrimaryRelayRequestValidator(var rules: MutableList<PrimaryRelayRequestRule>) {
    fun validate(item: PrimaryRelayRequest): MutableList<String> {
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

    fun requireValid(item: PrimaryRelayRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRelayRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRelayRequestValidator(): PrimaryRelayRequestValidator {
    return PrimaryRelayRequestValidator(
        mutableListOf<PrimaryRelayRequestRule>(
            PrimaryRelayRequestRequiredTitleRule("primary relay request needs a value"),
            PrimaryRelayRequestWeightRangeRule(2.3, 2848.6),
            PrimaryRelayRequestRequiredChecksumRule("primary relay request needs a value"),
            PrimaryRelayRequestNameLengthRule(3600),
        ),
    )
}
