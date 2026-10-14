package com.acme.transport.safety.nearmiss.rollingmissrecord

/** A rule a near miss score must pass. Check returns its problems, or none. */
interface NearMissScoreRule {
    fun check(item: NearMissScore): MutableList<String>
}

data class NearMissScoreEtaMinutesRangeRule(var min: Int, var max: Int) : NearMissScoreRule {
    override fun check(item: NearMissScore): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissScoreCarrierLengthRule(var maxLength: Int) : NearMissScoreRule {
    override fun check(item: NearMissScore): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissScoreCodeLengthRule(var maxLength: Int) : NearMissScoreRule {
    override fun check(item: NearMissScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissScoreRequiredNameRule(var message: String) : NearMissScoreRule {
    override fun check(item: NearMissScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss score. */
data class NearMissScoreValidator(var rules: MutableList<NearMissScoreRule>) {
    fun validate(item: NearMissScore): MutableList<String> {
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

    fun requireValid(item: NearMissScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissScore>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissScoreValidator(): NearMissScoreValidator {
    return NearMissScoreValidator(
        mutableListOf<NearMissScoreRule>(
            NearMissScoreEtaMinutesRangeRule(16, 1000),
            NearMissScoreCarrierLengthRule(3600),
            NearMissScoreCodeLengthRule(120),
            NearMissScoreRequiredNameRule("near miss score needs this field"),
        ),
    )
}
