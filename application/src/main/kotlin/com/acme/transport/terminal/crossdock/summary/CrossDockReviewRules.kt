package com.acme.transport.terminal.crossdock.summary

/** A rule a cross dock review must pass. Check returns its problems, or none. */
interface CrossDockReviewRule {
    fun check(item: CrossDockReview): MutableList<String>
}

data class CrossDockReviewRateRangeRule(var min: Double, var max: Double) : CrossDockReviewRule {
    override fun check(item: CrossDockReview): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockReviewUnitLengthRule(var maxLength: Int) : CrossDockReviewRule {
    override fun check(item: CrossDockReview): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockReviewRequiredCodeRule(var message: String) : CrossDockReviewRule {
    override fun check(item: CrossDockReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockReviewCodeLengthRule(var maxLength: Int) : CrossDockReviewRule {
    override fun check(item: CrossDockReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockReviewPalletsRangeRule(var min: Int, var max: Int) : CrossDockReviewRule {
    override fun check(item: CrossDockReview): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock review. */
data class CrossDockReviewValidator(var rules: MutableList<CrossDockReviewRule>) {
    fun validate(item: CrossDockReview): MutableList<String> {
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

    fun requireValid(item: CrossDockReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockReview>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockReviewValidator(): CrossDockReviewValidator {
    return CrossDockReviewValidator(
        mutableListOf<CrossDockReviewRule>(
            CrossDockReviewRateRangeRule(10.0, 3020.8),
            CrossDockReviewUnitLengthRule(86400),
            CrossDockReviewRequiredCodeRule("cross dock review needs this field"),
            CrossDockReviewCodeLengthRule(32),
            CrossDockReviewPalletsRangeRule(32, 255),
        ),
    )
}
