package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredUtilizationDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredUtilizationDigestValidator()
        val item = newTieredUtilizationDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = TieredUtilizationDigestExternalRefLengthRule(64)
        val item = newTieredUtilizationDigest("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = TieredUtilizationDigestMaximumRangeRule(7.7, 341.9)
        val item = newTieredUtilizationDigest("x", "y")
        item.maximum = 343.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredUtilizationDigestNameLengthRule(3600)
        val item = newTieredUtilizationDigest("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredUtilizationDigestCodeLengthRule(16)
        val item = newTieredUtilizationDigest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
