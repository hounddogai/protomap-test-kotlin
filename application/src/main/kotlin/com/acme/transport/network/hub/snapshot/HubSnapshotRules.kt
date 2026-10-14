package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a hub snapshot must pass. Check returns its problems, or none. */
interface HubSnapshotRule {
    fun check(item: HubSnapshot): MutableList<String>
}

data class HubSnapshotCeilingRangeRule(var min: Double, var max: Double) : HubSnapshotRule {
    override fun check(item: HubSnapshot): MutableList<String> {
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

data class HubSnapshotLaneLengthRule(var maxLength: Int) : HubSnapshotRule {
    override fun check(item: HubSnapshot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubSnapshotTitleLengthRule(var maxLength: Int) : HubSnapshotRule {
    override fun check(item: HubSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubSnapshotRequiredTitleRule(var message: String) : HubSnapshotRule {
    override fun check(item: HubSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubSnapshotAllowedTitleRule(var allowed: MutableList<String>) : HubSnapshotRule {
    override fun check(item: HubSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub snapshot. */
data class HubSnapshotValidator(var rules: MutableList<HubSnapshotRule>) {
    fun validate(item: HubSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultHubSnapshotValidator(): HubSnapshotValidator {
    return HubSnapshotValidator(
        mutableListOf<HubSnapshotRule>(
            HubSnapshotCeilingRangeRule(8.1, 3903.0),
            HubSnapshotLaneLengthRule(120),
            HubSnapshotTitleLengthRule(120),
            HubSnapshotRequiredTitleRule("hub snapshot needs a value"),
            HubSnapshotAllowedTitleRule(mutableListOf<String>("primary", "lenient", "weighted")),
        ),
    )
}
