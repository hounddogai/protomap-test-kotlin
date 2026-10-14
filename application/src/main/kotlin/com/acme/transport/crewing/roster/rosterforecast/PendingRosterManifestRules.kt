package com.acme.transport.crewing.roster.rosterforecast

/** A rule a pending roster manifest must pass. Check returns its problems, or none. */
interface PendingRosterManifestRule {
    fun check(item: PendingRosterManifest): MutableList<String>
}

data class PendingRosterManifestWeightRangeRule(var min: Double, var max: Double) : PendingRosterManifestRule {
    override fun check(item: PendingRosterManifest): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class PendingRosterManifestOwnerIdLengthRule(var maxLength: Int) : PendingRosterManifestRule {
    override fun check(item: PendingRosterManifest): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterManifestLabelLengthRule(var maxLength: Int) : PendingRosterManifestRule {
    override fun check(item: PendingRosterManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterManifestRetriesRangeRule(var min: Int, var max: Int) : PendingRosterManifestRule {
    override fun check(item: PendingRosterManifest): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending roster manifest. */
data class PendingRosterManifestValidator(var rules: MutableList<PendingRosterManifestRule>) {
    fun validate(item: PendingRosterManifest): MutableList<String> {
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

    fun requireValid(item: PendingRosterManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRosterManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRosterManifestValidator(): PendingRosterManifestValidator {
    return PendingRosterManifestValidator(
        mutableListOf<PendingRosterManifestRule>(
            PendingRosterManifestWeightRangeRule(8.3, 4427.9),
            PendingRosterManifestOwnerIdLengthRule(80),
            PendingRosterManifestLabelLengthRule(64),
            PendingRosterManifestRetriesRangeRule(120, 255),
        ),
    )
}
