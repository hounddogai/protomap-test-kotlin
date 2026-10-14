package com.acme.transport.network.hub.milestone

/** A rule a hub variance must pass. Check returns its problems, or none. */
interface HubVarianceRule {
    fun check(item: HubVariance): MutableList<String>
}

data class HubVarianceRatioRangeRule(var min: Double, var max: Double) : HubVarianceRule {
    override fun check(item: HubVariance): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubVarianceNameLengthRule(var maxLength: Int) : HubVarianceRule {
    override fun check(item: HubVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubVarianceCodeLengthRule(var maxLength: Int) : HubVarianceRule {
    override fun check(item: HubVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubVarianceRequiredDockRule(var message: String) : HubVarianceRule {
    override fun check(item: HubVariance): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubVarianceRequiredCodeRule(var message: String) : HubVarianceRule {
    override fun check(item: HubVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub variance. */
data class HubVarianceValidator(var rules: MutableList<HubVarianceRule>) {
    fun validate(item: HubVariance): MutableList<String> {
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

    fun requireValid(item: HubVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultHubVarianceValidator(): HubVarianceValidator {
    return HubVarianceValidator(
        mutableListOf<HubVarianceRule>(
            HubVarianceRatioRangeRule(4.6, 902.6),
            HubVarianceNameLengthRule(32),
            HubVarianceCodeLengthRule(64),
            HubVarianceRequiredDockRule("hub variance needs this field"),
            HubVarianceRequiredCodeRule("hub variance needs this field"),
        ),
    )
}
