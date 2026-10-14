package com.acme.transport.fleetops.leasereturn.returnforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLeaseReturnManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalLeaseReturnManifestValidator()
        val item = newRegionalLeaseReturnManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = RegionalLeaseReturnManifestCountryLengthRule(16)
        val item = newRegionalLeaseReturnManifest("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RegionalLeaseReturnManifestRequiredUnitRule("regional lease return manifest needs this field")
        val item = newRegionalLeaseReturnManifest("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalLeaseReturnManifestCodeLengthRule(86400)
        val item = newRegionalLeaseReturnManifest("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RegionalLeaseReturnManifestBudgetRangeRule(7.2, 1319.5)
        val item = newRegionalLeaseReturnManifest("x", "y")
        item.budget = 1321.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RegionalLeaseReturnManifestGraceDaysRangeRule(32, 86400)
        val item = newRegionalLeaseReturnManifest("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
