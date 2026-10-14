package com.acme.transport.terminal.sortation.tier

/** A rule a sortation manifest must pass. Check returns its problems, or none. */
interface SortationManifestRule {
    fun check(item: SortationManifest): MutableList<String>
}

data class SortationManifestRequiredDockRule(var message: String) : SortationManifestRule {
    override fun check(item: SortationManifest): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationManifestThresholdRangeRule(var min: Double, var max: Double) : SortationManifestRule {
    override fun check(item: SortationManifest): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class SortationManifestTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : SortationManifestRule {
    override fun check(item: SortationManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class SortationManifestCodePrefixRule(var prefix: String) : SortationManifestRule {
    override fun check(item: SortationManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationManifestRequiredCodeRule(var message: String) : SortationManifestRule {
    override fun check(item: SortationManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationManifestLabelLengthRule(var maxLength: Int) : SortationManifestRule {
    override fun check(item: SortationManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation manifest. */
data class SortationManifestValidator(var rules: MutableList<SortationManifestRule>) {
    fun validate(item: SortationManifest): MutableList<String> {
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

    fun requireValid(item: SortationManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationManifestValidator(): SortationManifestValidator {
    return SortationManifestValidator(
        mutableListOf<SortationManifestRule>(
            SortationManifestRequiredDockRule("sortation manifest needs an entry"),
            SortationManifestThresholdRangeRule(3.7, 4631.2),
            SortationManifestTagsLimitRule(64, mutableListOf<String>("seasonal", "batched")),
            SortationManifestCodePrefixRule("ACM"),
            SortationManifestRequiredCodeRule("sortation manifest needs this field"),
            SortationManifestLabelLengthRule(64),
        ),
    )
}
