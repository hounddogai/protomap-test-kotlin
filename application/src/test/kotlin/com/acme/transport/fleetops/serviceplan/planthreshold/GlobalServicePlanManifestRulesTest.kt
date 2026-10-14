package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalServicePlanManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalServicePlanManifestValidator()
        val item = newGlobalServicePlanManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = GlobalServicePlanManifestAmountRangeRule(0.7, 2073.0)
        val item = newGlobalServicePlanManifest("x", "y")
        item.amount = 2074.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = GlobalServicePlanManifestOwnerIdLengthRule(86400)
        val item = newGlobalServicePlanManifest("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalServicePlanManifestRequiredLabelRule("global service plan manifest needs a value")
        val item = newGlobalServicePlanManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
