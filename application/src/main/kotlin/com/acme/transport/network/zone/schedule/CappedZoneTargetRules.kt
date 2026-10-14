package com.acme.transport.network.zone.schedule

/** A rule a capped zone target must pass. Check returns its problems, or none. */
interface CappedZoneTargetRule {
    fun check(item: CappedZoneTarget): MutableList<String>
}

data class CappedZoneTargetDescriptionLengthRule(var maxLength: Int) : CappedZoneTargetRule {
    override fun check(item: CappedZoneTarget): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneTargetCodeLengthRule(var maxLength: Int) : CappedZoneTargetRule {
    override fun check(item: CappedZoneTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneTargetPhoneLengthRule(var maxLength: Int) : CappedZoneTargetRule {
    override fun check(item: CappedZoneTarget): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped zone target. */
data class CappedZoneTargetValidator(var rules: MutableList<CappedZoneTargetRule>) {
    fun validate(item: CappedZoneTarget): MutableList<String> {
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

    fun requireValid(item: CappedZoneTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedZoneTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedZoneTargetValidator(): CappedZoneTargetValidator {
    return CappedZoneTargetValidator(
        mutableListOf<CappedZoneTargetRule>(
            CappedZoneTargetDescriptionLengthRule(64),
            CappedZoneTargetCodeLengthRule(16),
            CappedZoneTargetPhoneLengthRule(80),
        ),
    )
}
