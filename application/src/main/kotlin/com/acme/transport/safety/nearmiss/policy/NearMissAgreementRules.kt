package com.acme.transport.safety.nearmiss.policy

/** A rule a near miss agreement must pass. Check returns its problems, or none. */
interface NearMissAgreementRule {
    fun check(item: NearMissAgreement): MutableList<String>
}

data class NearMissAgreementFloorValueRangeRule(var min: Double, var max: Double) : NearMissAgreementRule {
    override fun check(item: NearMissAgreement): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class NearMissAgreementNameLengthRule(var maxLength: Int) : NearMissAgreementRule {
    override fun check(item: NearMissAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAgreementRevisionRangeRule(var min: Int, var max: Int) : NearMissAgreementRule {
    override fun check(item: NearMissAgreement): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class NearMissAgreementCodeLengthRule(var maxLength: Int) : NearMissAgreementRule {
    override fun check(item: NearMissAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAgreementRequiredCodeRule(var message: String) : NearMissAgreementRule {
    override fun check(item: NearMissAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss agreement. */
data class NearMissAgreementValidator(var rules: MutableList<NearMissAgreementRule>) {
    fun validate(item: NearMissAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissAgreementValidator(): NearMissAgreementValidator {
    return NearMissAgreementValidator(
        mutableListOf<NearMissAgreementRule>(
            NearMissAgreementFloorValueRangeRule(6.6, 2629.8),
            NearMissAgreementNameLengthRule(120),
            NearMissAgreementRevisionRangeRule(32, 3600),
            NearMissAgreementCodeLengthRule(86400),
            NearMissAgreementRequiredCodeRule("near miss agreement needs an entry"),
        ),
    )
}
