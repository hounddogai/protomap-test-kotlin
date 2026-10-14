package com.acme.transport.fleetops.tire.tireagreement

/** A rule a primary tire group must pass. Check returns its problems, or none. */
interface PrimaryTireGroupRule {
    fun check(item: PrimaryTireGroup): MutableList<String>
}

data class PrimaryTireGroupPhoneLengthRule(var maxLength: Int) : PrimaryTireGroupRule {
    override fun check(item: PrimaryTireGroup): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireGroupSequenceRangeRule(var min: Int, var max: Int) : PrimaryTireGroupRule {
    override fun check(item: PrimaryTireGroup): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class PrimaryTireGroupRequiredPhoneRule(var message: String) : PrimaryTireGroupRule {
    override fun check(item: PrimaryTireGroup): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireGroupRequiredNameRule(var message: String) : PrimaryTireGroupRule {
    override fun check(item: PrimaryTireGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireGroupDurationMinutesRangeRule(var min: Int, var max: Int) : PrimaryTireGroupRule {
    override fun check(item: PrimaryTireGroup): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tire group. */
data class PrimaryTireGroupValidator(var rules: MutableList<PrimaryTireGroupRule>) {
    fun validate(item: PrimaryTireGroup): MutableList<String> {
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

    fun requireValid(item: PrimaryTireGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTireGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTireGroupValidator(): PrimaryTireGroupValidator {
    return PrimaryTireGroupValidator(
        mutableListOf<PrimaryTireGroupRule>(
            PrimaryTireGroupPhoneLengthRule(64),
            PrimaryTireGroupSequenceRangeRule(16, 255),
            PrimaryTireGroupRequiredPhoneRule("primary tire group needs a value"),
            PrimaryTireGroupRequiredNameRule("primary tire group needs a value"),
            PrimaryTireGroupDurationMinutesRangeRule(32, 64),
        ),
    )
}
