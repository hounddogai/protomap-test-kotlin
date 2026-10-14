package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a regional defect snapshot must pass. Check returns its problems, or none. */
interface RegionalDefectSnapshotRule {
    fun check(item: RegionalDefectSnapshot): MutableList<String>
}

data class RegionalDefectSnapshotRequiredRegionRule(var message: String) : RegionalDefectSnapshotRule {
    override fun check(item: RegionalDefectSnapshot): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectSnapshotLabelLengthRule(var maxLength: Int) : RegionalDefectSnapshotRule {
    override fun check(item: RegionalDefectSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectSnapshotRequiredDriverIdRule(var message: String) : RegionalDefectSnapshotRule {
    override fun check(item: RegionalDefectSnapshot): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectSnapshotAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : RegionalDefectSnapshotRule {
    override fun check(item: RegionalDefectSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectSnapshotRequiredLabelRule(var message: String) : RegionalDefectSnapshotRule {
    override fun check(item: RegionalDefectSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectSnapshotEndsAtRangeRule(var min: Int, var max: Int) : RegionalDefectSnapshotRule {
    override fun check(item: RegionalDefectSnapshot): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional defect snapshot. */
data class RegionalDefectSnapshotValidator(var rules: MutableList<RegionalDefectSnapshotRule>) {
    fun validate(item: RegionalDefectSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDefectSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDefectSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDefectSnapshotValidator(): RegionalDefectSnapshotValidator {
    return RegionalDefectSnapshotValidator(
        mutableListOf<RegionalDefectSnapshotRule>(
            RegionalDefectSnapshotRequiredRegionRule("regional defect snapshot needs an entry"),
            RegionalDefectSnapshotLabelLengthRule(80),
            RegionalDefectSnapshotRequiredDriverIdRule("regional defect snapshot needs this field"),
            RegionalDefectSnapshotAllowedDriverIdRule(mutableListOf<String>("tiered", "regional")),
            RegionalDefectSnapshotRequiredLabelRule("regional defect snapshot needs a value"),
            RegionalDefectSnapshotEndsAtRangeRule(64, 255),
        ),
    )
}
