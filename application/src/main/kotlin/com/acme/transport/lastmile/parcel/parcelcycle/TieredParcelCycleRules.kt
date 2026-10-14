package com.acme.transport.lastmile.parcel.parcelcycle

/** A rule a tiered parcel cycle must pass. Check returns its problems, or none. */
interface TieredParcelCycleRule {
    fun check(item: TieredParcelCycle): MutableList<String>
}

data class TieredParcelCycleWindowDaysRangeRule(var min: Int, var max: Int) : TieredParcelCycleRule {
    override fun check(item: TieredParcelCycle): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class TieredParcelCycleRequiredSourceRule(var message: String) : TieredParcelCycleRule {
    override fun check(item: TieredParcelCycle): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelCycleCodeLengthRule(var maxLength: Int) : TieredParcelCycleRule {
    override fun check(item: TieredParcelCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelCycleRequiredTitleRule(var message: String) : TieredParcelCycleRule {
    override fun check(item: TieredParcelCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelCycleRequiredContactEmailRule(var message: String) : TieredParcelCycleRule {
    override fun check(item: TieredParcelCycle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered parcel cycle. */
data class TieredParcelCycleValidator(var rules: MutableList<TieredParcelCycleRule>) {
    fun validate(item: TieredParcelCycle): MutableList<String> {
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

    fun requireValid(item: TieredParcelCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredParcelCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredParcelCycleValidator(): TieredParcelCycleValidator {
    return TieredParcelCycleValidator(
        mutableListOf<TieredParcelCycleRule>(
            TieredParcelCycleWindowDaysRangeRule(32, 32),
            TieredParcelCycleRequiredSourceRule("tiered parcel cycle needs an entry"),
            TieredParcelCycleCodeLengthRule(3600),
            TieredParcelCycleRequiredTitleRule("tiered parcel cycle needs an entry"),
            TieredParcelCycleRequiredContactEmailRule("tiered parcel cycle needs this field"),
        ),
    )
}
