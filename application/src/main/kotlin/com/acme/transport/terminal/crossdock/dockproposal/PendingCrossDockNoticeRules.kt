package com.acme.transport.terminal.crossdock.dockproposal

/** A rule a pending cross dock notice must pass. Check returns its problems, or none. */
interface PendingCrossDockNoticeRule {
    fun check(item: PendingCrossDockNotice): MutableList<String>
}

data class PendingCrossDockNoticeStopCountRangeRule(var min: Int, var max: Int) : PendingCrossDockNoticeRule {
    override fun check(item: PendingCrossDockNotice): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockNoticeCodePrefixRule(var prefix: String) : PendingCrossDockNoticeRule {
    override fun check(item: PendingCrossDockNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockNoticeCostRangeRule(var min: Double, var max: Double) : PendingCrossDockNoticeRule {
    override fun check(item: PendingCrossDockNotice): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class PendingCrossDockNoticeRequiredCodeRule(var message: String) : PendingCrossDockNoticeRule {
    override fun check(item: PendingCrossDockNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockNoticeLabelLengthRule(var maxLength: Int) : PendingCrossDockNoticeRule {
    override fun check(item: PendingCrossDockNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending cross dock notice. */
data class PendingCrossDockNoticeValidator(var rules: MutableList<PendingCrossDockNoticeRule>) {
    fun validate(item: PendingCrossDockNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCrossDockNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCrossDockNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCrossDockNoticeValidator(): PendingCrossDockNoticeValidator {
    return PendingCrossDockNoticeValidator(
        mutableListOf<PendingCrossDockNoticeRule>(
            PendingCrossDockNoticeStopCountRangeRule(32, 1000),
            PendingCrossDockNoticeCodePrefixRule("X"),
            PendingCrossDockNoticeCostRangeRule(2.7, 676.4),
            PendingCrossDockNoticeRequiredCodeRule("pending cross dock notice needs a value"),
            PendingCrossDockNoticeLabelLengthRule(255),
        ),
    )
}
