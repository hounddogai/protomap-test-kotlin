package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.text.normalize

/** A rule a capped near miss record must pass. Check returns its problems, or none. */
interface CappedNearMissRecordRule {
    fun check(item: CappedNearMissRecord): MutableList<String>
}

data class CappedNearMissRecordPhoneLengthRule(var maxLength: Int) : CappedNearMissRecordRule {
    override fun check(item: CappedNearMissRecord): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissRecordRequiredPhoneRule(var message: String) : CappedNearMissRecordRule {
    override fun check(item: CappedNearMissRecord): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissRecordCeilingRangeRule(var min: Double, var max: Double) : CappedNearMissRecordRule {
    override fun check(item: CappedNearMissRecord): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissRecordAllowedCodeRule(var allowed: MutableList<String>) : CappedNearMissRecordRule {
    override fun check(item: CappedNearMissRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissRecordRequiredCodeRule(var message: String) : CappedNearMissRecordRule {
    override fun check(item: CappedNearMissRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped near miss record. */
data class CappedNearMissRecordValidator(var rules: MutableList<CappedNearMissRecordRule>) {
    fun validate(item: CappedNearMissRecord): MutableList<String> {
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

    fun requireValid(item: CappedNearMissRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedNearMissRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedNearMissRecordValidator(): CappedNearMissRecordValidator {
    return CappedNearMissRecordValidator(
        mutableListOf<CappedNearMissRecordRule>(
            CappedNearMissRecordPhoneLengthRule(80),
            CappedNearMissRecordRequiredPhoneRule("capped near miss record needs a value"),
            CappedNearMissRecordCeilingRangeRule(5.2, 3100.8),
            CappedNearMissRecordAllowedCodeRule(mutableListOf<String>("capped", "manual")),
            CappedNearMissRecordRequiredCodeRule("capped near miss record needs an entry"),
        ),
    )
}
