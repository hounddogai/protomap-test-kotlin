package com.acme.transport.network.cutoff.segment

/** A rule a cutoff assignment must pass. Check returns its problems, or none. */
interface CutoffAssignmentRule {
    fun check(item: CutoffAssignment): MutableList<String>
}

data class CutoffAssignmentCodeLengthRule(var maxLength: Int) : CutoffAssignmentRule {
    override fun check(item: CutoffAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffAssignmentRequiredCodeRule(var message: String) : CutoffAssignmentRule {
    override fun check(item: CutoffAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffAssignmentRequiredChannelRule(var message: String) : CutoffAssignmentRule {
    override fun check(item: CutoffAssignment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffAssignmentRequiredLaneRule(var message: String) : CutoffAssignmentRule {
    override fun check(item: CutoffAssignment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff assignment. */
data class CutoffAssignmentValidator(var rules: MutableList<CutoffAssignmentRule>) {
    fun validate(item: CutoffAssignment): MutableList<String> {
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

    fun requireValid(item: CutoffAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffAssignmentValidator(): CutoffAssignmentValidator {
    return CutoffAssignmentValidator(
        mutableListOf<CutoffAssignmentRule>(
            CutoffAssignmentCodeLengthRule(64),
            CutoffAssignmentRequiredCodeRule("cutoff assignment needs this field"),
            CutoffAssignmentRequiredChannelRule("cutoff assignment needs an entry"),
            CutoffAssignmentRequiredLaneRule("cutoff assignment needs a value"),
        ),
    )
}
