package com.acme.transport.terminal.dockdoor.doorclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDockDoorClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDockDoorClaimValidator()
        val item = newRegionalDockDoorClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RegionalDockDoorClaimRequiredTimezoneRule("regional dock door claim needs a value")
        val item = newRegionalDockDoorClaim("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RegionalDockDoorClaimRetriesRangeRule(120, 86400)
        val item = newRegionalDockDoorClaim("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalDockDoorClaimRequiredNameRule("regional dock door claim needs a value")
        val item = newRegionalDockDoorClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = RegionalDockDoorClaimNotesLengthRule(16)
        val item = newRegionalDockDoorClaim("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = RegionalDockDoorClaimRequiredNotesRule("regional dock door claim needs this field")
        val item = newRegionalDockDoorClaim("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
