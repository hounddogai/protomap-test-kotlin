package com.acme.transport.network.zone.tier

/** A rule a zone rollup must pass. Check returns its problems, or none. */
interface ZoneRollupRule {
    fun check(item: ZoneRollup): MutableList<String>
}

data class ZoneRollupLocaleLengthRule(var maxLength: Int) : ZoneRollupRule {
    override fun check(item: ZoneRollup): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRollupRequiredSegmentRule(var message: String) : ZoneRollupRule {
    override fun check(item: ZoneRollup): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneRollupLabelLengthRule(var maxLength: Int) : ZoneRollupRule {
    override fun check(item: ZoneRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRollupCodeLengthRule(var maxLength: Int) : ZoneRollupRule {
    override fun check(item: ZoneRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone rollup. */
data class ZoneRollupValidator(var rules: MutableList<ZoneRollupRule>) {
    fun validate(item: ZoneRollup): MutableList<String> {
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

    fun requireValid(item: ZoneRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneRollupValidator(): ZoneRollupValidator {
    return ZoneRollupValidator(
        mutableListOf<ZoneRollupRule>(
            ZoneRollupLocaleLengthRule(80),
            ZoneRollupRequiredSegmentRule("zone rollup needs an entry"),
            ZoneRollupLabelLengthRule(1000),
            ZoneRollupCodeLengthRule(80),
        ),
    )
}
