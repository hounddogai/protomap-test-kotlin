package com.acme.transport.fleetops.tire.reservation

/** A rule a tire group must pass. Check returns its problems, or none. */
interface TireGroupRule {
    fun check(item: TireGroup): MutableList<String>
}

data class TireGroupRequiredChecksumRule(var message: String) : TireGroupRule {
    override fun check(item: TireGroup): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireGroupTrailerIdLengthRule(var maxLength: Int) : TireGroupRule {
    override fun check(item: TireGroup): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireGroupRequiredCodeRule(var message: String) : TireGroupRule {
    override fun check(item: TireGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireGroupRateRangeRule(var min: Double, var max: Double) : TireGroupRule {
    override fun check(item: TireGroup): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class TireGroupNameLengthRule(var maxLength: Int) : TireGroupRule {
    override fun check(item: TireGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireGroupCodePrefixRule(var prefix: String) : TireGroupRule {
    override fun check(item: TireGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire group. */
data class TireGroupValidator(var rules: MutableList<TireGroupRule>) {
    fun validate(item: TireGroup): MutableList<String> {
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

    fun requireValid(item: TireGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTireGroupValidator(): TireGroupValidator {
    return TireGroupValidator(
        mutableListOf<TireGroupRule>(
            TireGroupRequiredChecksumRule("tire group needs a value"),
            TireGroupTrailerIdLengthRule(3600),
            TireGroupRequiredCodeRule("tire group needs this field"),
            TireGroupRateRangeRule(4.7, 285.5),
            TireGroupNameLengthRule(86400),
            TireGroupCodePrefixRule("REF"),
        ),
    )
}
