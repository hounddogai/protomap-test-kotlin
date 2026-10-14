package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.text.normalize

/** A rule a swap notice must pass. Check returns its problems, or none. */
interface SwapNoticeRule {
    fun check(item: SwapNotice): MutableList<String>
}

data class SwapNoticeBatchSizeRangeRule(var min: Int, var max: Int) : SwapNoticeRule {
    override fun check(item: SwapNotice): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapNoticeNameLengthRule(var maxLength: Int) : SwapNoticeRule {
    override fun check(item: SwapNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapNoticeRequiredTitleRule(var message: String) : SwapNoticeRule {
    override fun check(item: SwapNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapNoticeCodeLengthRule(var maxLength: Int) : SwapNoticeRule {
    override fun check(item: SwapNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapNoticeAllowedTitleRule(var allowed: MutableList<String>) : SwapNoticeRule {
    override fun check(item: SwapNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap notice. */
data class SwapNoticeValidator(var rules: MutableList<SwapNoticeRule>) {
    fun validate(item: SwapNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapNoticeValidator(): SwapNoticeValidator {
    return SwapNoticeValidator(
        mutableListOf<SwapNoticeRule>(
            SwapNoticeBatchSizeRangeRule(64, 120),
            SwapNoticeNameLengthRule(64),
            SwapNoticeRequiredTitleRule("swap notice needs a value"),
            SwapNoticeCodeLengthRule(1000),
            SwapNoticeAllowedTitleRule(mutableListOf<String>("strict", "global")),
        ),
    )
}
