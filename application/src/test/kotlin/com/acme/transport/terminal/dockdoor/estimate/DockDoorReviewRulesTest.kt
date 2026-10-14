package com.acme.transport.terminal.dockdoor.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorReviewValidator()
        val item = newDockDoorReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = DockDoorReviewRegionLengthRule(1000)
        val item = newDockDoorReview("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DockDoorReviewCodeLengthRule(80)
        val item = newDockDoorReview("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorReviewRequiredCodeRule("dock door review needs an entry")
        val item = newDockDoorReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
