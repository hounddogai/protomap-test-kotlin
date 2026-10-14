package com.acme.transport.network.servicearea.exception

/** A rule a service area exception must pass. Check returns its problems, or none. */
interface ServiceAreaExceptionRule {
    fun check(item: ServiceAreaException): MutableList<String>
}

data class ServiceAreaExceptionRequiredCodeRule(var message: String) : ServiceAreaExceptionRule {
    override fun check(item: ServiceAreaException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaExceptionTitleLengthRule(var maxLength: Int) : ServiceAreaExceptionRule {
    override fun check(item: ServiceAreaException): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaExceptionCodePrefixRule(var prefix: String) : ServiceAreaExceptionRule {
    override fun check(item: ServiceAreaException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area exception. */
data class ServiceAreaExceptionValidator(var rules: MutableList<ServiceAreaExceptionRule>) {
    fun validate(item: ServiceAreaException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaException>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaExceptionValidator(): ServiceAreaExceptionValidator {
    return ServiceAreaExceptionValidator(
        mutableListOf<ServiceAreaExceptionRule>(
            ServiceAreaExceptionRequiredCodeRule("service area exception needs an entry"),
            ServiceAreaExceptionTitleLengthRule(16),
            ServiceAreaExceptionCodePrefixRule("REF"),
        ),
    )
}
