package com.acme.transport.network.node.schedule

/** A rule a pending node bundle must pass. Check returns its problems, or none. */
interface PendingNodeBundleRule {
    fun check(item: PendingNodeBundle): MutableList<String>
}

data class PendingNodeBundleRequiredNameRule(var message: String) : PendingNodeBundleRule {
    override fun check(item: PendingNodeBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeBundleRequiredCodeRule(var message: String) : PendingNodeBundleRule {
    override fun check(item: PendingNodeBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeBundleExpiresAtRangeRule(var min: Int, var max: Int) : PendingNodeBundleRule {
    override fun check(item: PendingNodeBundle): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeBundleRequiredOwnerIdRule(var message: String) : PendingNodeBundleRule {
    override fun check(item: PendingNodeBundle): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeBundleOwnerIdLengthRule(var maxLength: Int) : PendingNodeBundleRule {
    override fun check(item: PendingNodeBundle): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeBundleCodeLengthRule(var maxLength: Int) : PendingNodeBundleRule {
    override fun check(item: PendingNodeBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending node bundle. */
data class PendingNodeBundleValidator(var rules: MutableList<PendingNodeBundleRule>) {
    fun validate(item: PendingNodeBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingNodeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNodeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNodeBundleValidator(): PendingNodeBundleValidator {
    return PendingNodeBundleValidator(
        mutableListOf<PendingNodeBundleRule>(
            PendingNodeBundleRequiredNameRule("pending node bundle needs this field"),
            PendingNodeBundleRequiredCodeRule("pending node bundle needs an entry"),
            PendingNodeBundleExpiresAtRangeRule(32, 64),
            PendingNodeBundleRequiredOwnerIdRule("pending node bundle needs a value"),
            PendingNodeBundleOwnerIdLengthRule(80),
            PendingNodeBundleCodeLengthRule(86400),
        ),
    )
}
