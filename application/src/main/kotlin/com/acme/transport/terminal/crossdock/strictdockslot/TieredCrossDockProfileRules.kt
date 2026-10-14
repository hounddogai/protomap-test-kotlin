package com.acme.transport.terminal.crossdock.strictdockslot

/** A rule a tiered cross dock profile must pass. Check returns its problems, or none. */
interface TieredCrossDockProfileRule {
    fun check(item: TieredCrossDockProfile): MutableList<String>
}

data class TieredCrossDockProfileRequiredCodeRule(var message: String) : TieredCrossDockProfileRule {
    override fun check(item: TieredCrossDockProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockProfileFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : TieredCrossDockProfileRule {
    override fun check(item: TieredCrossDockProfile): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockProfileCodeLengthRule(var maxLength: Int) : TieredCrossDockProfileRule {
    override fun check(item: TieredCrossDockProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockProfileChannelLengthRule(var maxLength: Int) : TieredCrossDockProfileRule {
    override fun check(item: TieredCrossDockProfile): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockProfileRequiredDriverIdRule(var message: String) : TieredCrossDockProfileRule {
    override fun check(item: TieredCrossDockProfile): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered cross dock profile. */
data class TieredCrossDockProfileValidator(var rules: MutableList<TieredCrossDockProfileRule>) {
    fun validate(item: TieredCrossDockProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCrossDockProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCrossDockProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCrossDockProfileValidator(): TieredCrossDockProfileValidator {
    return TieredCrossDockProfileValidator(
        mutableListOf<TieredCrossDockProfileRule>(
            TieredCrossDockProfileRequiredCodeRule("tiered cross dock profile needs an entry"),
            TieredCrossDockProfileFloorValueRangeRule(2.6, 3471.5),
            TieredCrossDockProfileCodeLengthRule(16),
            TieredCrossDockProfileChannelLengthRule(64),
            TieredCrossDockProfileRequiredDriverIdRule("tiered cross dock profile needs an entry"),
        ),
    )
}
