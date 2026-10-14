package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.text.normalize

/** A rule a primary camera event record must pass. Check returns its problems, or none. */
interface PrimaryCameraEventRecordRule {
    fun check(item: PrimaryCameraEventRecord): MutableList<String>
}

data class PrimaryCameraEventRecordAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : PrimaryCameraEventRecordRule {
    override fun check(item: PrimaryCameraEventRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventRecordChannelLengthRule(var maxLength: Int) : PrimaryCameraEventRecordRule {
    override fun check(item: PrimaryCameraEventRecord): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventRecordExternalRefLengthRule(var maxLength: Int) : PrimaryCameraEventRecordRule {
    override fun check(item: PrimaryCameraEventRecord): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventRecordOdometerRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryCameraEventRecordRule {
    override fun check(item: PrimaryCameraEventRecord): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class PrimaryCameraEventRecordTitleLengthRule(var maxLength: Int) : PrimaryCameraEventRecordRule {
    override fun check(item: PrimaryCameraEventRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary camera event record. */
data class PrimaryCameraEventRecordValidator(var rules: MutableList<PrimaryCameraEventRecordRule>) {
    fun validate(item: PrimaryCameraEventRecord): MutableList<String> {
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

    fun requireValid(item: PrimaryCameraEventRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCameraEventRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCameraEventRecordValidator(): PrimaryCameraEventRecordValidator {
    return PrimaryCameraEventRecordValidator(
        mutableListOf<PrimaryCameraEventRecordRule>(
            PrimaryCameraEventRecordAllowedExternalRefRule(mutableListOf<String>("global")),
            PrimaryCameraEventRecordChannelLengthRule(86400),
            PrimaryCameraEventRecordExternalRefLengthRule(86400),
            PrimaryCameraEventRecordOdometerRangeRule(64, 80),
            PrimaryCameraEventRecordTitleLengthRule(255),
        ),
    )
}
