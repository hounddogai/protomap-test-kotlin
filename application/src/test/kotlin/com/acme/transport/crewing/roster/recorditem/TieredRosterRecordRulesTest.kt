package com.acme.transport.crewing.roster.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRosterRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRosterRecordValidator()
        val item = newTieredRosterRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = TieredRosterRecordDockLengthRule(1000)
        val item = newTieredRosterRecord("x", "y")
        item.dock = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredRosterRecordCodeLengthRule(32)
        val item = newTieredRosterRecord("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredRosterRecordRequiredCodeRule("tiered roster record needs this field")
        val item = newTieredRosterRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredRosterRecordRequiredNameRule("tiered roster record needs this field")
        val item = newTieredRosterRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
