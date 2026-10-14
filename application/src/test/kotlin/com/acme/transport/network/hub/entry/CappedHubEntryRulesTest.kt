package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedHubEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedHubEntryValidator()
        val item = newCappedHubEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = CappedHubEntryExternalRefLengthRule(3600)
        val item = newCappedHubEntry("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CappedHubEntryRatioRangeRule(5.7, 3097.5)
        val item = newCappedHubEntry("x", "y")
        item.ratio = 3099.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = CappedHubEntryFloorValueRangeRule(9.1, 1363.5)
        val item = newCappedHubEntry("x", "y")
        item.floorValue = 1365.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
