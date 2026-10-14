package com.acme.transport.customs.declaration.estimate

/** A rule a weighted declaration claim must pass. Check returns its problems, or none. */
interface WeightedDeclarationClaimRule {
    fun check(item: WeightedDeclarationClaim): MutableList<String>
}

data class WeightedDeclarationClaimRequiredChannelRule(var message: String) : WeightedDeclarationClaimRule {
    override fun check(item: WeightedDeclarationClaim): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationClaimCodeLengthRule(var maxLength: Int) : WeightedDeclarationClaimRule {
    override fun check(item: WeightedDeclarationClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationClaimRequiredDriverIdRule(var message: String) : WeightedDeclarationClaimRule {
    override fun check(item: WeightedDeclarationClaim): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationClaimRequiredLabelRule(var message: String) : WeightedDeclarationClaimRule {
    override fun check(item: WeightedDeclarationClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationClaimVisibleGuardRule(var limit: Int) : WeightedDeclarationClaimRule {
    override fun check(item: WeightedDeclarationClaim): MutableList<String> {
        if (item.visible && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted declaration claim. */
data class WeightedDeclarationClaimValidator(var rules: MutableList<WeightedDeclarationClaimRule>) {
    fun validate(item: WeightedDeclarationClaim): MutableList<String> {
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

    fun requireValid(item: WeightedDeclarationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDeclarationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDeclarationClaimValidator(): WeightedDeclarationClaimValidator {
    return WeightedDeclarationClaimValidator(
        mutableListOf<WeightedDeclarationClaimRule>(
            WeightedDeclarationClaimRequiredChannelRule("weighted declaration claim needs a value"),
            WeightedDeclarationClaimCodeLengthRule(80),
            WeightedDeclarationClaimRequiredDriverIdRule("weighted declaration claim needs a value"),
            WeightedDeclarationClaimRequiredLabelRule("weighted declaration claim needs this field"),
            WeightedDeclarationClaimVisibleGuardRule(86400),
        ),
    )
}
