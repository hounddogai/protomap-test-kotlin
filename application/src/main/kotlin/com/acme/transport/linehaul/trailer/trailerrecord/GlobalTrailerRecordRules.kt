package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.text.normalize

/** A rule a global trailer record must pass. Check returns its problems, or none. */
interface GlobalTrailerRecordRule {
    fun check(item: GlobalTrailerRecord): MutableList<String>
}

data class GlobalTrailerRecordRequiredNameRule(var message: String) : GlobalTrailerRecordRule {
    override fun check(item: GlobalTrailerRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerRecordSummaryLengthRule(var maxLength: Int) : GlobalTrailerRecordRule {
    override fun check(item: GlobalTrailerRecord): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerRecordTimezoneLengthRule(var maxLength: Int) : GlobalTrailerRecordRule {
    override fun check(item: GlobalTrailerRecord): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerRecordAllowedNameRule(var allowed: MutableList<String>) : GlobalTrailerRecordRule {
    override fun check(item: GlobalTrailerRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global trailer record. */
data class GlobalTrailerRecordValidator(var rules: MutableList<GlobalTrailerRecordRule>) {
    fun validate(item: GlobalTrailerRecord): MutableList<String> {
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

    fun requireValid(item: GlobalTrailerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerRecordValidator(): GlobalTrailerRecordValidator {
    return GlobalTrailerRecordValidator(
        mutableListOf<GlobalTrailerRecordRule>(
            GlobalTrailerRecordRequiredNameRule("global trailer record needs this field"),
            GlobalTrailerRecordSummaryLengthRule(80),
            GlobalTrailerRecordTimezoneLengthRule(120),
            GlobalTrailerRecordAllowedNameRule(mutableListOf<String>("regional")),
        ),
    )
}
