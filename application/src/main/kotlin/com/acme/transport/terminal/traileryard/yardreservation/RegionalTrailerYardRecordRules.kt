package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.normalize

/** A rule a regional trailer yard record must pass. Check returns its problems, or none. */
interface RegionalTrailerYardRecordRule {
    fun check(item: RegionalTrailerYardRecord): MutableList<String>
}

data class RegionalTrailerYardRecordRegionLengthRule(var maxLength: Int) : RegionalTrailerYardRecordRule {
    override fun check(item: RegionalTrailerYardRecord): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardRecordAllowedTitleRule(
    var allowed: MutableList<String>,
) : RegionalTrailerYardRecordRule {
    override fun check(item: RegionalTrailerYardRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardRecordTitleLengthRule(var maxLength: Int) : RegionalTrailerYardRecordRule {
    override fun check(item: RegionalTrailerYardRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer yard record. */
data class RegionalTrailerYardRecordValidator(var rules: MutableList<RegionalTrailerYardRecordRule>) {
    fun validate(item: RegionalTrailerYardRecord): MutableList<String> {
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

    fun requireValid(item: RegionalTrailerYardRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerYardRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerYardRecordValidator(): RegionalTrailerYardRecordValidator {
    return RegionalTrailerYardRecordValidator(
        mutableListOf<RegionalTrailerYardRecordRule>(
            RegionalTrailerYardRecordRegionLengthRule(32),
            RegionalTrailerYardRecordAllowedTitleRule(mutableListOf<String>("seasonal", "manual")),
            RegionalTrailerYardRecordTitleLengthRule(32),
        ),
    )
}
