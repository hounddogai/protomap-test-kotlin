package com.acme.transport.lastmile.drop.preference

/** A rule a drop manifest must pass. Check returns its problems, or none. */
interface DropManifestRule {
    fun check(item: DropManifest): MutableList<String>
}

data class DropManifestNotesLengthRule(var maxLength: Int) : DropManifestRule {
    override fun check(item: DropManifest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropManifestLabelLengthRule(var maxLength: Int) : DropManifestRule {
    override fun check(item: DropManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropManifestCreatedAtRangeRule(var min: Int, var max: Int) : DropManifestRule {
    override fun check(item: DropManifest): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropManifestCodePrefixRule(var prefix: String) : DropManifestRule {
    override fun check(item: DropManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropManifestStartsAtRangeRule(var min: Int, var max: Int) : DropManifestRule {
    override fun check(item: DropManifest): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class DropManifestLaneLengthRule(var maxLength: Int) : DropManifestRule {
    override fun check(item: DropManifest): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop manifest. */
data class DropManifestValidator(var rules: MutableList<DropManifestRule>) {
    fun validate(item: DropManifest): MutableList<String> {
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

    fun requireValid(item: DropManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDropManifestValidator(): DropManifestValidator {
    return DropManifestValidator(
        mutableListOf<DropManifestRule>(
            DropManifestNotesLengthRule(64),
            DropManifestLabelLengthRule(64),
            DropManifestCreatedAtRangeRule(16, 255),
            DropManifestCodePrefixRule("OPS"),
            DropManifestStartsAtRangeRule(120, 3600),
            DropManifestLaneLengthRule(120),
        ),
    )
}
