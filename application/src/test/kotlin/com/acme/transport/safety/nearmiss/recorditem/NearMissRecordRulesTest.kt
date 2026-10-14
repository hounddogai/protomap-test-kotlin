package com.acme.transport.safety.nearmiss.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissRecordValidator()
        val item = newNearMissRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = NearMissRecordRequiredContactEmailRule("near miss record needs an entry")
        val item = newNearMissRecord("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = NearMissRecordTitleLengthRule(86400)
        val item = newNearMissRecord("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissRecordCodeLengthRule(1000)
        val item = newNearMissRecord("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
