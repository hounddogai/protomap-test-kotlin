package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.text.normalize

/** A rule a weighted lane plan bundle must pass. Check returns its problems, or none. */
interface WeightedLanePlanBundleRule {
    fun check(item: WeightedLanePlanBundle): MutableList<String>
}

data class WeightedLanePlanBundleRequiredCodeRule(var message: String) : WeightedLanePlanBundleRule {
    override fun check(item: WeightedLanePlanBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanBundleSequenceRangeRule(var min: Int, var max: Int) : WeightedLanePlanBundleRule {
    override fun check(item: WeightedLanePlanBundle): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanBundleAllowedDockRule(
    var allowed: MutableList<String>,
) : WeightedLanePlanBundleRule {
    override fun check(item: WeightedLanePlanBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanBundleRequiredCarrierRule(var message: String) : WeightedLanePlanBundleRule {
    override fun check(item: WeightedLanePlanBundle): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted lane plan bundle. */
data class WeightedLanePlanBundleValidator(var rules: MutableList<WeightedLanePlanBundleRule>) {
    fun validate(item: WeightedLanePlanBundle): MutableList<String> {
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

    fun requireValid(item: WeightedLanePlanBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLanePlanBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLanePlanBundleValidator(): WeightedLanePlanBundleValidator {
    return WeightedLanePlanBundleValidator(
        mutableListOf<WeightedLanePlanBundleRule>(
            WeightedLanePlanBundleRequiredCodeRule("weighted lane plan bundle needs an entry"),
            WeightedLanePlanBundleSequenceRangeRule(86400, 86400),
            WeightedLanePlanBundleAllowedDockRule(mutableListOf<String>("adaptive", "batched")),
            WeightedLanePlanBundleRequiredCarrierRule("weighted lane plan bundle needs an entry"),
        ),
    )
}
