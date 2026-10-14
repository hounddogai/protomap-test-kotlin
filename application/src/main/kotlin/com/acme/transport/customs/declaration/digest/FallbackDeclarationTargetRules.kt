package com.acme.transport.customs.declaration.digest

/** A rule a fallback declaration target must pass. Check returns its problems, or none. */
interface FallbackDeclarationTargetRule {
    fun check(item: FallbackDeclarationTarget): MutableList<String>
}

data class FallbackDeclarationTargetDiscountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackDeclarationTargetRule {
    override fun check(item: FallbackDeclarationTarget): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class FallbackDeclarationTargetRequiredRegionRule(var message: String) : FallbackDeclarationTargetRule {
    override fun check(item: FallbackDeclarationTarget): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationTargetTrailerIdLengthRule(var maxLength: Int) : FallbackDeclarationTargetRule {
    override fun check(item: FallbackDeclarationTarget): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationTargetCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackDeclarationTargetRule {
    override fun check(item: FallbackDeclarationTarget): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback declaration target. */
data class FallbackDeclarationTargetValidator(var rules: MutableList<FallbackDeclarationTargetRule>) {
    fun validate(item: FallbackDeclarationTarget): MutableList<String> {
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

    fun requireValid(item: FallbackDeclarationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDeclarationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDeclarationTargetValidator(): FallbackDeclarationTargetValidator {
    return FallbackDeclarationTargetValidator(
        mutableListOf<FallbackDeclarationTargetRule>(
            FallbackDeclarationTargetDiscountRangeRule(6.4, 2792.6),
            FallbackDeclarationTargetRequiredRegionRule("fallback declaration target needs an entry"),
            FallbackDeclarationTargetTrailerIdLengthRule(86400),
            FallbackDeclarationTargetCreatedAtRangeRule(32, 64),
        ),
    )
}
