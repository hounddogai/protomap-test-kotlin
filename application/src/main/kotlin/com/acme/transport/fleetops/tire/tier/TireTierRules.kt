package com.acme.transport.fleetops.tire.tier

/** A rule a tire tier must pass. Check returns its problems, or none. */
interface TireTierRule {
    fun check(item: TireTier): MutableList<String>
}

data class TireTierTrailerIdLengthRule(var maxLength: Int) : TireTierRule {
    override fun check(item: TireTier): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireTierNameLengthRule(var maxLength: Int) : TireTierRule {
    override fun check(item: TireTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireTierCodeLengthRule(var maxLength: Int) : TireTierRule {
    override fun check(item: TireTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireTierPriorityRangeRule(var min: Int, var max: Int) : TireTierRule {
    override fun check(item: TireTier): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class TireTierRequiredCodeRule(var message: String) : TireTierRule {
    override fun check(item: TireTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire tier. */
data class TireTierValidator(var rules: MutableList<TireTierRule>) {
    fun validate(item: TireTier): MutableList<String> {
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

    fun requireValid(item: TireTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireTier>): MutableMap<String, MutableList<String>> {
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

fun defaultTireTierValidator(): TireTierValidator {
    return TireTierValidator(
        mutableListOf<TireTierRule>(
            TireTierTrailerIdLengthRule(32),
            TireTierNameLengthRule(32),
            TireTierCodeLengthRule(3600),
            TireTierPriorityRangeRule(32, 255),
            TireTierRequiredCodeRule("tire tier needs an entry"),
        ),
    )
}
