package com.acme.transport.crewing.qualification.bundle

/** A rule a global qualification bundle must pass. Check returns its problems, or none. */
interface GlobalQualificationBundleRule {
    fun check(item: GlobalQualificationBundle): MutableList<String>
}

data class GlobalQualificationBundleCodePrefixRule(var prefix: String) : GlobalQualificationBundleRule {
    override fun check(item: GlobalQualificationBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationBundleArchivedGuardRule(var limit: Int) : GlobalQualificationBundleRule {
    override fun check(item: GlobalQualificationBundle): MutableList<String> {
        if (item.archived && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationBundleFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : GlobalQualificationBundleRule {
    override fun check(item: GlobalQualificationBundle): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class GlobalQualificationBundleRequiredExternalRefRule(
    var message: String,
) : GlobalQualificationBundleRule {
    override fun check(item: GlobalQualificationBundle): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global qualification bundle. */
data class GlobalQualificationBundleValidator(var rules: MutableList<GlobalQualificationBundleRule>) {
    fun validate(item: GlobalQualificationBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalQualificationBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalQualificationBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalQualificationBundleValidator(): GlobalQualificationBundleValidator {
    return GlobalQualificationBundleValidator(
        mutableListOf<GlobalQualificationBundleRule>(
            GlobalQualificationBundleCodePrefixRule("ACM"),
            GlobalQualificationBundleArchivedGuardRule(120),
            GlobalQualificationBundleFloorValueRangeRule(8.5, 1055.0),
            GlobalQualificationBundleRequiredExternalRefRule("global qualification bundle needs a value"),
        ),
    )
}
