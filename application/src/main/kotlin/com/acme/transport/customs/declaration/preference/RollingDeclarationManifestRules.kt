package com.acme.transport.customs.declaration.preference

/** A rule a rolling declaration manifest must pass. Check returns its problems, or none. */
interface RollingDeclarationManifestRule {
    fun check(item: RollingDeclarationManifest): MutableList<String>
}

data class RollingDeclarationManifestRequiredCodeRule(var message: String) : RollingDeclarationManifestRule {
    override fun check(item: RollingDeclarationManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationManifestSummaryLengthRule(var maxLength: Int) : RollingDeclarationManifestRule {
    override fun check(item: RollingDeclarationManifest): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationManifestRequiredShiftCodeRule(
    var message: String,
) : RollingDeclarationManifestRule {
    override fun check(item: RollingDeclarationManifest): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationManifestFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : RollingDeclarationManifestRule {
    override fun check(item: RollingDeclarationManifest): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling declaration manifest. */
data class RollingDeclarationManifestValidator(var rules: MutableList<RollingDeclarationManifestRule>) {
    fun validate(item: RollingDeclarationManifest): MutableList<String> {
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

    fun requireValid(item: RollingDeclarationManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDeclarationManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDeclarationManifestValidator(): RollingDeclarationManifestValidator {
    return RollingDeclarationManifestValidator(
        mutableListOf<RollingDeclarationManifestRule>(
            RollingDeclarationManifestRequiredCodeRule("rolling declaration manifest needs this field"),
            RollingDeclarationManifestSummaryLengthRule(64),
            RollingDeclarationManifestRequiredShiftCodeRule("rolling declaration manifest needs this field"),
            RollingDeclarationManifestFloorValueRangeRule(1.6, 2072.7),
        ),
    )
}
