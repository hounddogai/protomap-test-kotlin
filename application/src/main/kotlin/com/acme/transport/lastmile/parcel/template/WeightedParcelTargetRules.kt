package com.acme.transport.lastmile.parcel.template

/** A rule a weighted parcel target must pass. Check returns its problems, or none. */
interface WeightedParcelTargetRule {
    fun check(item: WeightedParcelTarget): MutableList<String>
}

data class WeightedParcelTargetRequiredNameRule(var message: String) : WeightedParcelTargetRule {
    override fun check(item: WeightedParcelTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelTargetApprovedGuardRule(var limit: Int) : WeightedParcelTargetRule {
    override fun check(item: WeightedParcelTarget): MutableList<String> {
        if (item.approved && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelTargetVersionRangeRule(var min: Int, var max: Int) : WeightedParcelTargetRule {
    override fun check(item: WeightedParcelTarget): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelTargetPalletsRangeRule(var min: Int, var max: Int) : WeightedParcelTargetRule {
    override fun check(item: WeightedParcelTarget): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelTargetRequiredRegionRule(var message: String) : WeightedParcelTargetRule {
    override fun check(item: WeightedParcelTarget): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted parcel target. */
data class WeightedParcelTargetValidator(var rules: MutableList<WeightedParcelTargetRule>) {
    fun validate(item: WeightedParcelTarget): MutableList<String> {
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

    fun requireValid(item: WeightedParcelTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedParcelTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedParcelTargetValidator(): WeightedParcelTargetValidator {
    return WeightedParcelTargetValidator(
        mutableListOf<WeightedParcelTargetRule>(
            WeightedParcelTargetRequiredNameRule("weighted parcel target needs an entry"),
            WeightedParcelTargetApprovedGuardRule(80),
            WeightedParcelTargetVersionRangeRule(32, 1000),
            WeightedParcelTargetPalletsRangeRule(32, 64),
            WeightedParcelTargetRequiredRegionRule("weighted parcel target needs a value"),
        ),
    )
}
