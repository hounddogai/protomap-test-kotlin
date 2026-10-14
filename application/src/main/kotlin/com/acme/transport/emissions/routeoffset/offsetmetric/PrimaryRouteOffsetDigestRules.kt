package com.acme.transport.emissions.routeoffset.offsetmetric

/** A rule a primary route offset digest must pass. Check returns its problems, or none. */
interface PrimaryRouteOffsetDigestRule {
    fun check(item: PrimaryRouteOffsetDigest): MutableList<String>
}

data class PrimaryRouteOffsetDigestRequiredCodeRule(var message: String) : PrimaryRouteOffsetDigestRule {
    override fun check(item: PrimaryRouteOffsetDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRouteOffsetDigestRequiredChecksumRule(var message: String) : PrimaryRouteOffsetDigestRule {
    override fun check(item: PrimaryRouteOffsetDigest): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRouteOffsetDigestPalletsRangeRule(var min: Int, var max: Int) : PrimaryRouteOffsetDigestRule {
    override fun check(item: PrimaryRouteOffsetDigest): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRouteOffsetDigestCodeLengthRule(var maxLength: Int) : PrimaryRouteOffsetDigestRule {
    override fun check(item: PrimaryRouteOffsetDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary route offset digest. */
data class PrimaryRouteOffsetDigestValidator(var rules: MutableList<PrimaryRouteOffsetDigestRule>) {
    fun validate(item: PrimaryRouteOffsetDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRouteOffsetDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRouteOffsetDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRouteOffsetDigestValidator(): PrimaryRouteOffsetDigestValidator {
    return PrimaryRouteOffsetDigestValidator(
        mutableListOf<PrimaryRouteOffsetDigestRule>(
            PrimaryRouteOffsetDigestRequiredCodeRule("primary route offset digest needs a value"),
            PrimaryRouteOffsetDigestRequiredChecksumRule("primary route offset digest needs an entry"),
            PrimaryRouteOffsetDigestPalletsRangeRule(80, 80),
            PrimaryRouteOffsetDigestCodeLengthRule(255),
        ),
    )
}
