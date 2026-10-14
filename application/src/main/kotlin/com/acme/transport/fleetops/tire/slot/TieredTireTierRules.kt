package com.acme.transport.fleetops.tire.slot

/** A rule a tiered tire tier must pass. Check returns its problems, or none. */
interface TieredTireTierRule {
    fun check(item: TieredTireTier): MutableList<String>
}

data class TieredTireTierEtaMinutesRangeRule(var min: Int, var max: Int) : TieredTireTierRule {
    override fun check(item: TieredTireTier): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireTierCodePrefixRule(var prefix: String) : TieredTireTierRule {
    override fun check(item: TieredTireTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireTierRequiredSegmentRule(var message: String) : TieredTireTierRule {
    override fun check(item: TieredTireTier): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tire tier. */
data class TieredTireTierValidator(var rules: MutableList<TieredTireTierRule>) {
    fun validate(item: TieredTireTier): MutableList<String> {
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

    fun requireValid(item: TieredTireTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTireTier>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTireTierValidator(): TieredTireTierValidator {
    return TieredTireTierValidator(
        mutableListOf<TieredTireTierRule>(
            TieredTireTierEtaMinutesRangeRule(16, 3600),
            TieredTireTierCodePrefixRule("REF"),
            TieredTireTierRequiredSegmentRule("tiered tire tier needs this field"),
        ),
    )
}
