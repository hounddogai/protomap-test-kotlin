package com.acme.transport.lastmile.drop.dropmilestone

/** A rule a rolling drop digest must pass. Check returns its problems, or none. */
interface RollingDropDigestRule {
    fun check(item: RollingDropDigest): MutableList<String>
}

data class RollingDropDigestDockLengthRule(var maxLength: Int) : RollingDropDigestRule {
    override fun check(item: RollingDropDigest): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDropDigestRequiredCarrierRule(var message: String) : RollingDropDigestRule {
    override fun check(item: RollingDropDigest): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDropDigestCodeLengthRule(var maxLength: Int) : RollingDropDigestRule {
    override fun check(item: RollingDropDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling drop digest. */
data class RollingDropDigestValidator(var rules: MutableList<RollingDropDigestRule>) {
    fun validate(item: RollingDropDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDropDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDropDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDropDigestValidator(): RollingDropDigestValidator {
    return RollingDropDigestValidator(
        mutableListOf<RollingDropDigestRule>(
            RollingDropDigestDockLengthRule(120),
            RollingDropDigestRequiredCarrierRule("rolling drop digest needs a value"),
            RollingDropDigestCodeLengthRule(16),
        ),
    )
}
