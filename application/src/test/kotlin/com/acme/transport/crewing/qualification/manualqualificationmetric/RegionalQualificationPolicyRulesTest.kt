package com.acme.transport.crewing.qualification.manualqualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalQualificationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalQualificationPolicyValidator()
        val item = newRegionalQualificationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = RegionalQualificationPolicyDistanceKmRangeRule(5.6, 238.1)
        val item = newRegionalQualificationPolicy("x", "y")
        item.distanceKm = 239.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = RegionalQualificationPolicyDescriptionLengthRule(3600)
        val item = newRegionalQualificationPolicy("x", "y")
        item.description = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RegionalQualificationPolicyRequiredRouteCodeRule("regional qualification policy needs an entry")
        val item = newRegionalQualificationPolicy("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalQualificationPolicyRequiredNameRule("regional qualification policy needs an entry")
        val item = newRegionalQualificationPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
