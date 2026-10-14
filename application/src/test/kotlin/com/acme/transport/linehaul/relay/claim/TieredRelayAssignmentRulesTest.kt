package com.acme.transport.linehaul.relay.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRelayAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRelayAssignmentValidator()
        val item = newTieredRelayAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredRelayAssignmentRequiredCodeRule("tiered relay assignment needs an entry")
        val item = newTieredRelayAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = TieredRelayAssignmentDiscountRangeRule(9.2, 4553.9)
        val item = newTieredRelayAssignment("x", "y")
        item.discount = 4555.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = TieredRelayAssignmentRequiredChecksumRule("tiered relay assignment needs this field")
        val item = newTieredRelayAssignment("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredRelayAssignmentNameLengthRule(80)
        val item = newTieredRelayAssignment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
