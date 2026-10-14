package com.acme.transport.safety.cameraevent.rollup

/** A rule a manual camera event digest must pass. Check returns its problems, or none. */
interface ManualCameraEventDigestRule {
    fun check(item: ManualCameraEventDigest): MutableList<String>
}

data class ManualCameraEventDigestRequiredSegmentRule(var message: String) : ManualCameraEventDigestRule {
    override fun check(item: ManualCameraEventDigest): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventDigestPriorityRangeRule(var min: Int, var max: Int) : ManualCameraEventDigestRule {
    override fun check(item: ManualCameraEventDigest): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class ManualCameraEventDigestRequiredLabelRule(var message: String) : ManualCameraEventDigestRule {
    override fun check(item: ManualCameraEventDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventDigestCodeLengthRule(var maxLength: Int) : ManualCameraEventDigestRule {
    override fun check(item: ManualCameraEventDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventDigestRequiredCategoryRule(var message: String) : ManualCameraEventDigestRule {
    override fun check(item: ManualCameraEventDigest): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual camera event digest. */
data class ManualCameraEventDigestValidator(var rules: MutableList<ManualCameraEventDigestRule>) {
    fun validate(item: ManualCameraEventDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualCameraEventDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCameraEventDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCameraEventDigestValidator(): ManualCameraEventDigestValidator {
    return ManualCameraEventDigestValidator(
        mutableListOf<ManualCameraEventDigestRule>(
            ManualCameraEventDigestRequiredSegmentRule("manual camera event digest needs a value"),
            ManualCameraEventDigestPriorityRangeRule(64, 1000),
            ManualCameraEventDigestRequiredLabelRule("manual camera event digest needs an entry"),
            ManualCameraEventDigestCodeLengthRule(120),
            ManualCameraEventDigestRequiredCategoryRule("manual camera event digest needs this field"),
        ),
    )
}
