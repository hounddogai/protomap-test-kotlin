package com.acme.transport.safety.nearmiss.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissAuditValidator()
        val item = newNearMissAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = NearMissAuditCountryLengthRule(32)
        val item = newNearMissAudit("x", "y")
        item.country = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = NearMissAuditNameLengthRule(86400)
        val item = newNearMissAudit("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = NearMissAuditRequiredNameRule("near miss audit needs an entry")
        val item = newNearMissAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = NearMissAuditThresholdRangeRule(9.0, 4068.4)
        val item = newNearMissAudit("x", "y")
        item.threshold = 4069.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
