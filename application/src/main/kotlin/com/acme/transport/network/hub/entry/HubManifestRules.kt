package com.acme.transport.network.hub.entry

/** A rule a hub manifest must pass. Check returns its problems, or none. */
interface HubManifestRule {
    fun check(item: HubManifest): MutableList<String>
}

data class HubManifestRequiredLabelRule(var message: String) : HubManifestRule {
    override fun check(item: HubManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubManifestSegmentLengthRule(var maxLength: Int) : HubManifestRule {
    override fun check(item: HubManifest): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubManifestCategoryLengthRule(var maxLength: Int) : HubManifestRule {
    override fun check(item: HubManifest): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubManifestRequiredSegmentRule(var message: String) : HubManifestRule {
    override fun check(item: HubManifest): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubManifestEndsAtRangeRule(var min: Int, var max: Int) : HubManifestRule {
    override fun check(item: HubManifest): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub manifest. */
data class HubManifestValidator(var rules: MutableList<HubManifestRule>) {
    fun validate(item: HubManifest): MutableList<String> {
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

    fun requireValid(item: HubManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultHubManifestValidator(): HubManifestValidator {
    return HubManifestValidator(
        mutableListOf<HubManifestRule>(
            HubManifestRequiredLabelRule("hub manifest needs a value"),
            HubManifestSegmentLengthRule(80),
            HubManifestCategoryLengthRule(64),
            HubManifestRequiredSegmentRule("hub manifest needs an entry"),
            HubManifestEndsAtRangeRule(16, 1000),
        ),
    )
}
