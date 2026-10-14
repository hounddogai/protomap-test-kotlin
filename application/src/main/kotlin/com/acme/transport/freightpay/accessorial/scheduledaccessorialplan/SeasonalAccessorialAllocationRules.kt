package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.text.normalize

/** A rule a seasonal accessorial allocation must pass. Check returns its problems, or none. */
interface SeasonalAccessorialAllocationRule {
    fun check(item: SeasonalAccessorialAllocation): MutableList<String>
}

data class SeasonalAccessorialAllocationVisibleGuardRule(
    var limit: Double,
) : SeasonalAccessorialAllocationRule {
    override fun check(item: SeasonalAccessorialAllocation): MutableList<String> {
        if (item.visible && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialAllocationCeilingRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalAccessorialAllocationRule {
    override fun check(item: SeasonalAccessorialAllocation): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class SeasonalAccessorialAllocationRequiredCarrierRule(
    var message: String,
) : SeasonalAccessorialAllocationRule {
    override fun check(item: SeasonalAccessorialAllocation): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalAccessorialAllocationRule {
    override fun check(item: SeasonalAccessorialAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialAllocationRequiredCodeRule(
    var message: String,
) : SeasonalAccessorialAllocationRule {
    override fun check(item: SeasonalAccessorialAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal accessorial allocation. */
data class SeasonalAccessorialAllocationValidator(var rules: MutableList<SeasonalAccessorialAllocationRule>) {
    fun validate(item: SeasonalAccessorialAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAccessorialAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAccessorialAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAccessorialAllocationValidator(): SeasonalAccessorialAllocationValidator {
    return SeasonalAccessorialAllocationValidator(
        mutableListOf<SeasonalAccessorialAllocationRule>(
            SeasonalAccessorialAllocationVisibleGuardRule(1.4),
            SeasonalAccessorialAllocationCeilingRangeRule(0.6, 1049.6),
            SeasonalAccessorialAllocationRequiredCarrierRule("seasonal accessorial allocation needs a value"),
            SeasonalAccessorialAllocationAllowedCodeRule(mutableListOf<String>()),
            SeasonalAccessorialAllocationRequiredCodeRule("seasonal accessorial allocation needs an entry"),
        ),
    )
}
