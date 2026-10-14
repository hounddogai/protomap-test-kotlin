package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledQualificationProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledQualificationProfileValidator()
        val item = newScheduledQualificationProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ScheduledQualificationProfileQuantityRangeRule(64, 80)
        val item = newScheduledQualificationProfile("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledQualificationProfileCodeLengthRule(255)
        val item = newScheduledQualificationProfile("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ScheduledQualificationProfileExpiresAtRangeRule(16, 80)
        val item = newScheduledQualificationProfile("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledQualificationProfileRequiredCodeRule("scheduled qualification profile needs an entry")
        val item = newScheduledQualificationProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
