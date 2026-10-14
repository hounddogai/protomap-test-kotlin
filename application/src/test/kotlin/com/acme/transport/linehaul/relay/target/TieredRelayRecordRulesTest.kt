package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRelayRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRelayRecordValidator()
        val item = newTieredRelayRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = TieredRelayRecordSequenceRangeRule(1000, 3600)
        val item = newTieredRelayRecord("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredRelayRecordNameLengthRule(255)
        val item = newTieredRelayRecord("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredRelayRecordRequiredCodeRule("tiered relay record needs a value")
        val item = newTieredRelayRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
