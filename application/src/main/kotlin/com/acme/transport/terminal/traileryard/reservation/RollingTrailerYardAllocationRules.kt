package com.acme.transport.terminal.traileryard.reservation

/** A rule a rolling trailer yard allocation must pass. Check returns its problems, or none. */
interface RollingTrailerYardAllocationRule {
    fun check(item: RollingTrailerYardAllocation): MutableList<String>
}

data class RollingTrailerYardAllocationNameLengthRule(var maxLength: Int) : RollingTrailerYardAllocationRule {
    override fun check(item: RollingTrailerYardAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardAllocationCodeLengthRule(var maxLength: Int) : RollingTrailerYardAllocationRule {
    override fun check(item: RollingTrailerYardAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardAllocationRequiredNameRule(
    var message: String,
) : RollingTrailerYardAllocationRule {
    override fun check(item: RollingTrailerYardAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardAllocationPriorityRangeRule(
    var min: Int,
    var max: Int,
) : RollingTrailerYardAllocationRule {
    override fun check(item: RollingTrailerYardAllocation): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling trailer yard allocation. */
data class RollingTrailerYardAllocationValidator(var rules: MutableList<RollingTrailerYardAllocationRule>) {
    fun validate(item: RollingTrailerYardAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingTrailerYardAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTrailerYardAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTrailerYardAllocationValidator(): RollingTrailerYardAllocationValidator {
    return RollingTrailerYardAllocationValidator(
        mutableListOf<RollingTrailerYardAllocationRule>(
            RollingTrailerYardAllocationNameLengthRule(32),
            RollingTrailerYardAllocationCodeLengthRule(255),
            RollingTrailerYardAllocationRequiredNameRule("rolling trailer yard allocation needs an entry"),
            RollingTrailerYardAllocationPriorityRangeRule(80, 3600),
        ),
    )
}
