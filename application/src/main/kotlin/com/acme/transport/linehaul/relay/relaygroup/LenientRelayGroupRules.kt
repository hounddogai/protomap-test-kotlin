package com.acme.transport.linehaul.relay.relaygroup

/** A rule a lenient relay group must pass. Check returns its problems, or none. */
interface LenientRelayGroupRule {
    fun check(item: LenientRelayGroup): MutableList<String>
}

data class LenientRelayGroupCodeLengthRule(var maxLength: Int) : LenientRelayGroupRule {
    override fun check(item: LenientRelayGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayGroupTitleLengthRule(var maxLength: Int) : LenientRelayGroupRule {
    override fun check(item: LenientRelayGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayGroupRequiredReviewerRule(var message: String) : LenientRelayGroupRule {
    override fun check(item: LenientRelayGroup): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayGroupReviewerLengthRule(var maxLength: Int) : LenientRelayGroupRule {
    override fun check(item: LenientRelayGroup): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient relay group. */
data class LenientRelayGroupValidator(var rules: MutableList<LenientRelayGroupRule>) {
    fun validate(item: LenientRelayGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRelayGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRelayGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRelayGroupValidator(): LenientRelayGroupValidator {
    return LenientRelayGroupValidator(
        mutableListOf<LenientRelayGroupRule>(
            LenientRelayGroupCodeLengthRule(32),
            LenientRelayGroupTitleLengthRule(1000),
            LenientRelayGroupRequiredReviewerRule("lenient relay group needs an entry"),
            LenientRelayGroupReviewerLengthRule(255),
        ),
    )
}
