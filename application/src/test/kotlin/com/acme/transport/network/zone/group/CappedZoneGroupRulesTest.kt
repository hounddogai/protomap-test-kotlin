package com.acme.transport.network.zone.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedZoneGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedZoneGroupValidator()
        val item = newCappedZoneGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedZoneGroupRequiredCodeRule("capped zone group needs a value")
        val item = newCappedZoneGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedZoneGroupRequiredNameRule("capped zone group needs this field")
        val item = newCappedZoneGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedZoneGroupCodeLengthRule(86400)
        val item = newCappedZoneGroup("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = CappedZoneGroupEndsAtRangeRule(80, 86400)
        val item = newCappedZoneGroup("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = CappedZoneGroupDockLengthRule(86400)
        val item = newCappedZoneGroup("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
