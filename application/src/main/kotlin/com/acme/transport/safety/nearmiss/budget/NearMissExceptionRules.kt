package com.acme.transport.safety.nearmiss.budget

/** A rule a near miss exception must pass. Check returns its problems, or none. */
interface NearMissExceptionRule {
    fun check(item: NearMissException): MutableList<String>
}

data class NearMissExceptionCodeLengthRule(var maxLength: Int) : NearMissExceptionRule {
    override fun check(item: NearMissException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissExceptionSegmentLengthRule(var maxLength: Int) : NearMissExceptionRule {
    override fun check(item: NearMissException): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissExceptionPalletsRangeRule(var min: Int, var max: Int) : NearMissExceptionRule {
    override fun check(item: NearMissException): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class NearMissExceptionNameLengthRule(var maxLength: Int) : NearMissExceptionRule {
    override fun check(item: NearMissException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissExceptionRequiredNameRule(var message: String) : NearMissExceptionRule {
    override fun check(item: NearMissException): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss exception. */
data class NearMissExceptionValidator(var rules: MutableList<NearMissExceptionRule>) {
    fun validate(item: NearMissException): MutableList<String> {
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

    fun requireValid(item: NearMissException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissException>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissExceptionValidator(): NearMissExceptionValidator {
    return NearMissExceptionValidator(
        mutableListOf<NearMissExceptionRule>(
            NearMissExceptionCodeLengthRule(64),
            NearMissExceptionSegmentLengthRule(64),
            NearMissExceptionPalletsRangeRule(120, 255),
            NearMissExceptionNameLengthRule(120),
            NearMissExceptionRequiredNameRule("near miss exception needs a value"),
        ),
    )
}
