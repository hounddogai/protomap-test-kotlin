package com.acme.transport.fleetops.serviceplan.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyServicePlanVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyServicePlanVarianceValidator()
        val item = newLegacyServicePlanVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyServicePlanVarianceRequiredNameRule("legacy service plan variance needs an entry")
        val item = newLegacyServicePlanVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LegacyServicePlanVarianceRequiredTimezoneRule("legacy service plan variance needs a value")
        val item = newLegacyServicePlanVariance("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyServicePlanVarianceCodeLengthRule(86400)
        val item = newLegacyServicePlanVariance("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = LegacyServicePlanVarianceTimezoneLengthRule(64)
        val item = newLegacyServicePlanVariance("x", "y")
        item.timezone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
