package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.normalize

/** A rule a parcel template must pass. Check returns its problems, or none. */
interface ParcelTemplateRule {
    fun check(item: ParcelTemplate): MutableList<String>
}

data class ParcelTemplateNameLengthRule(var maxLength: Int) : ParcelTemplateRule {
    override fun check(item: ParcelTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelTemplateRequiredCodeRule(var message: String) : ParcelTemplateRule {
    override fun check(item: ParcelTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelTemplateCodeLengthRule(var maxLength: Int) : ParcelTemplateRule {
    override fun check(item: ParcelTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelTemplateAllowedNameRule(var allowed: MutableList<String>) : ParcelTemplateRule {
    override fun check(item: ParcelTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ParcelTemplatePalletsRangeRule(var min: Int, var max: Int) : ParcelTemplateRule {
    override fun check(item: ParcelTemplate): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel template. */
data class ParcelTemplateValidator(var rules: MutableList<ParcelTemplateRule>) {
    fun validate(item: ParcelTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelTemplateValidator(): ParcelTemplateValidator {
    return ParcelTemplateValidator(
        mutableListOf<ParcelTemplateRule>(
            ParcelTemplateNameLengthRule(64),
            ParcelTemplateRequiredCodeRule("parcel template needs a value"),
            ParcelTemplateCodeLengthRule(32),
            ParcelTemplateAllowedNameRule(mutableListOf<String>("tiered")),
            ParcelTemplatePalletsRangeRule(16, 1000),
        ),
    )
}
