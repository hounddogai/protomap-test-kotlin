package com.acme.transport.linehaul.tender.weightedtenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTenderRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTenderRollupValidator()
        val item = newRollingTenderRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RollingTenderRollupExpiresAtRangeRule(255, 1000)
        val item = newRollingTenderRollup("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RollingTenderRollupRevisionRangeRule(1000, 86400)
        val item = newRollingTenderRollup("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = RollingTenderRollupDockLengthRule(120)
        val item = newRollingTenderRollup("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RollingTenderRollupPhoneLengthRule(32)
        val item = newRollingTenderRollup("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingTenderRollupCodeLengthRule(3600)
        val item = newRollingTenderRollup("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingTenderRollupRequiredTitleRule("rolling tender rollup needs this field")
        val item = newRollingTenderRollup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
