package com.acme.transport.terminal.crossdock.segment

/** A rule a cross dock request must pass. Check returns its problems, or none. */
interface CrossDockRequestRule {
    fun check(item: CrossDockRequest): MutableList<String>
}

data class CrossDockRequestLabelLengthRule(var maxLength: Int) : CrossDockRequestRule {
    override fun check(item: CrossDockRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRequestRequiredLabelRule(var message: String) : CrossDockRequestRule {
    override fun check(item: CrossDockRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRequestCeilingRangeRule(var min: Double, var max: Double) : CrossDockRequestRule {
    override fun check(item: CrossDockRequest): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRequestRatioRangeRule(var min: Double, var max: Double) : CrossDockRequestRule {
    override fun check(item: CrossDockRequest): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRequestCodeLengthRule(var maxLength: Int) : CrossDockRequestRule {
    override fun check(item: CrossDockRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock request. */
data class CrossDockRequestValidator(var rules: MutableList<CrossDockRequestRule>) {
    fun validate(item: CrossDockRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockRequestValidator(): CrossDockRequestValidator {
    return CrossDockRequestValidator(
        mutableListOf<CrossDockRequestRule>(
            CrossDockRequestLabelLengthRule(3600),
            CrossDockRequestRequiredLabelRule("cross dock request needs an entry"),
            CrossDockRequestCeilingRangeRule(0.7, 1463.5),
            CrossDockRequestRatioRangeRule(0.2, 3753.1),
            CrossDockRequestCodeLengthRule(80),
        ),
    )
}
