package com.acme.transport.network.node.profile

/** A rule a adaptive node threshold must pass. Check returns its problems, or none. */
interface AdaptiveNodeThresholdRule {
    fun check(item: AdaptiveNodeThreshold): MutableList<String>
}

data class AdaptiveNodeThresholdNameLengthRule(var maxLength: Int) : AdaptiveNodeThresholdRule {
    override fun check(item: AdaptiveNodeThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeThresholdRequiredCodeRule(var message: String) : AdaptiveNodeThresholdRule {
    override fun check(item: AdaptiveNodeThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeThresholdLocaleLengthRule(var maxLength: Int) : AdaptiveNodeThresholdRule {
    override fun check(item: AdaptiveNodeThreshold): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeThresholdCodeLengthRule(var maxLength: Int) : AdaptiveNodeThresholdRule {
    override fun check(item: AdaptiveNodeThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeThresholdTitleLengthRule(var maxLength: Int) : AdaptiveNodeThresholdRule {
    override fun check(item: AdaptiveNodeThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive node threshold. */
data class AdaptiveNodeThresholdValidator(var rules: MutableList<AdaptiveNodeThresholdRule>) {
    fun validate(item: AdaptiveNodeThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveNodeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNodeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNodeThresholdValidator(): AdaptiveNodeThresholdValidator {
    return AdaptiveNodeThresholdValidator(
        mutableListOf<AdaptiveNodeThresholdRule>(
            AdaptiveNodeThresholdNameLengthRule(16),
            AdaptiveNodeThresholdRequiredCodeRule("adaptive node threshold needs a value"),
            AdaptiveNodeThresholdLocaleLengthRule(255),
            AdaptiveNodeThresholdCodeLengthRule(80),
            AdaptiveNodeThresholdTitleLengthRule(255),
        ),
    )
}
