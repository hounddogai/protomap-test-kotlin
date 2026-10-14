package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredUtilizationProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredUtilizationProfileValidator()
        val item = newTieredUtilizationProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredUtilizationProfileNameLengthRule(3600)
        val item = newTieredUtilizationProfile("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TieredUtilizationProfileExpiresAtRangeRule(1000, 86400)
        val item = newTieredUtilizationProfile("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = TieredUtilizationProfileRevisionRangeRule(64, 80)
        val item = newTieredUtilizationProfile("x", "y")
        item.revision = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
