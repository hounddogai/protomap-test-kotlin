package com.acme.transport.lastmile.drop.milestone

/** A rule a drop segment must pass. Check returns its problems, or none. */
interface DropSegmentRule {
    fun check(item: DropSegment): MutableList<String>
}

data class DropSegmentAmountRangeRule(var min: Double, var max: Double) : DropSegmentRule {
    override fun check(item: DropSegment): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropSegmentNotesLengthRule(var maxLength: Int) : DropSegmentRule {
    override fun check(item: DropSegment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropSegmentNameLengthRule(var maxLength: Int) : DropSegmentRule {
    override fun check(item: DropSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropSegmentCodePrefixRule(var prefix: String) : DropSegmentRule {
    override fun check(item: DropSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropSegmentRequiredCodeRule(var message: String) : DropSegmentRule {
    override fun check(item: DropSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop segment. */
data class DropSegmentValidator(var rules: MutableList<DropSegmentRule>) {
    fun validate(item: DropSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDropSegmentValidator(): DropSegmentValidator {
    return DropSegmentValidator(
        mutableListOf<DropSegmentRule>(
            DropSegmentAmountRangeRule(1.3, 385.9),
            DropSegmentNotesLengthRule(80),
            DropSegmentNameLengthRule(1000),
            DropSegmentCodePrefixRule("ACM"),
            DropSegmentRequiredCodeRule("drop segment needs an entry"),
        ),
    )
}
