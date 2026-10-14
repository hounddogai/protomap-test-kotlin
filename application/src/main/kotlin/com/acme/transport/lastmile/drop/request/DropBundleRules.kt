package com.acme.transport.lastmile.drop.request

/** A rule a drop bundle must pass. Check returns its problems, or none. */
interface DropBundleRule {
    fun check(item: DropBundle): MutableList<String>
}

data class DropBundleLabelLengthRule(var maxLength: Int) : DropBundleRule {
    override fun check(item: DropBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropBundleRequiredLabelRule(var message: String) : DropBundleRule {
    override fun check(item: DropBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropBundleRequiredDescriptionRule(var message: String) : DropBundleRule {
    override fun check(item: DropBundle): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropBundleDurationMinutesRangeRule(var min: Int, var max: Int) : DropBundleRule {
    override fun check(item: DropBundle): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop bundle. */
data class DropBundleValidator(var rules: MutableList<DropBundleRule>) {
    fun validate(item: DropBundle): MutableList<String> {
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

    fun requireValid(item: DropBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultDropBundleValidator(): DropBundleValidator {
    return DropBundleValidator(
        mutableListOf<DropBundleRule>(
            DropBundleLabelLengthRule(1000),
            DropBundleRequiredLabelRule("drop bundle needs a value"),
            DropBundleRequiredDescriptionRule("drop bundle needs an entry"),
            DropBundleDurationMinutesRangeRule(80, 3600),
        ),
    )
}
