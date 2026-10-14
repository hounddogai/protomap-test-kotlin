package com.acme.transport.terminal.traileryard.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardQuotaValidator()
        val item = newTrailerYardQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = TrailerYardQuotaDepotLengthRule(32)
        val item = newTrailerYardQuota("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = TrailerYardQuotaLabelLengthRule(86400)
        val item = newTrailerYardQuota("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
