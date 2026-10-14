package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.text.normalize

/** A rule a service area digest must pass. Check returns its problems, or none. */
interface ServiceAreaDigestRule {
    fun check(item: ServiceAreaDigest): MutableList<String>
}

data class ServiceAreaDigestRequiredTitleRule(var message: String) : ServiceAreaDigestRule {
    override fun check(item: ServiceAreaDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaDigestAllowedTitleRule(var allowed: MutableList<String>) : ServiceAreaDigestRule {
    override fun check(item: ServiceAreaDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaDigestCodeLengthRule(var maxLength: Int) : ServiceAreaDigestRule {
    override fun check(item: ServiceAreaDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaDigestRequiredCodeRule(var message: String) : ServiceAreaDigestRule {
    override fun check(item: ServiceAreaDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaDigestVisibleGuardRule(var limit: Double) : ServiceAreaDigestRule {
    override fun check(item: ServiceAreaDigest): MutableList<String> {
        if (item.visible && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area digest. */
data class ServiceAreaDigestValidator(var rules: MutableList<ServiceAreaDigestRule>) {
    fun validate(item: ServiceAreaDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaDigestValidator(): ServiceAreaDigestValidator {
    return ServiceAreaDigestValidator(
        mutableListOf<ServiceAreaDigestRule>(
            ServiceAreaDigestRequiredTitleRule("service area digest needs a value"),
            ServiceAreaDigestAllowedTitleRule(mutableListOf<String>("rolling", "fallback", "adaptive")),
            ServiceAreaDigestCodeLengthRule(255),
            ServiceAreaDigestRequiredCodeRule("service area digest needs an entry"),
            ServiceAreaDigestVisibleGuardRule(3.0),
        ),
    )
}
