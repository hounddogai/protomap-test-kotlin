package com.acme.transport.linehaul.relay.relaythreshold

/** A rule a rolling relay tier must pass. Check returns its problems, or none. */
interface RollingRelayTierRule {
    fun check(item: RollingRelayTier): MutableList<String>
}

data class RollingRelayTierRequiredPhoneRule(var message: String) : RollingRelayTierRule {
    override fun check(item: RollingRelayTier): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayTierRequiredLaneRule(var message: String) : RollingRelayTierRule {
    override fun check(item: RollingRelayTier): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayTierPalletsRangeRule(var min: Int, var max: Int) : RollingRelayTierRule {
    override fun check(item: RollingRelayTier): MutableList<String> {
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

data class RollingRelayTierPhoneLengthRule(var maxLength: Int) : RollingRelayTierRule {
    override fun check(item: RollingRelayTier): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling relay tier. */
data class RollingRelayTierValidator(var rules: MutableList<RollingRelayTierRule>) {
    fun validate(item: RollingRelayTier): MutableList<String> {
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

    fun requireValid(item: RollingRelayTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRelayTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRelayTierValidator(): RollingRelayTierValidator {
    return RollingRelayTierValidator(
        mutableListOf<RollingRelayTierRule>(
            RollingRelayTierRequiredPhoneRule("rolling relay tier needs an entry"),
            RollingRelayTierRequiredLaneRule("rolling relay tier needs an entry"),
            RollingRelayTierPalletsRangeRule(120, 120),
            RollingRelayTierPhoneLengthRule(255),
        ),
    )
}
