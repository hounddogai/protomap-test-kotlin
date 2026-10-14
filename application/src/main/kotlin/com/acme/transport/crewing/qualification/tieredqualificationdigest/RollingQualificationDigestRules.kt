package com.acme.transport.crewing.qualification.tieredqualificationdigest

/** A rule a rolling qualification digest must pass. Check returns its problems, or none. */
interface RollingQualificationDigestRule {
    fun check(item: RollingQualificationDigest): MutableList<String>
}

data class RollingQualificationDigestCodePrefixRule(var prefix: String) : RollingQualificationDigestRule {
    override fun check(item: RollingQualificationDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingQualificationDigestPalletsRangeRule(
    var min: Int,
    var max: Int,
) : RollingQualificationDigestRule {
    override fun check(item: RollingQualificationDigest): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class RollingQualificationDigestRequiredNameRule(var message: String) : RollingQualificationDigestRule {
    override fun check(item: RollingQualificationDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingQualificationDigestNameLengthRule(var maxLength: Int) : RollingQualificationDigestRule {
    override fun check(item: RollingQualificationDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingQualificationDigestCapacityRangeRule(
    var min: Int,
    var max: Int,
) : RollingQualificationDigestRule {
    override fun check(item: RollingQualificationDigest): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling qualification digest. */
data class RollingQualificationDigestValidator(var rules: MutableList<RollingQualificationDigestRule>) {
    fun validate(item: RollingQualificationDigest): MutableList<String> {
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

    fun requireValid(item: RollingQualificationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingQualificationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingQualificationDigestValidator(): RollingQualificationDigestValidator {
    return RollingQualificationDigestValidator(
        mutableListOf<RollingQualificationDigestRule>(
            RollingQualificationDigestCodePrefixRule("OPS"),
            RollingQualificationDigestPalletsRangeRule(16, 3600),
            RollingQualificationDigestRequiredNameRule("rolling qualification digest needs a value"),
            RollingQualificationDigestNameLengthRule(3600),
            RollingQualificationDigestCapacityRangeRule(255, 3600),
        ),
    )
}
