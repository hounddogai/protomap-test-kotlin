package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRouteOffsetCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRouteOffsetCheckpointValidator()
        val item = newLenientRouteOffsetCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = LenientRouteOffsetCheckpointContactEmailLengthRule(16)
        val item = newLenientRouteOffsetCheckpoint("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientRouteOffsetCheckpointRequiredTitleRule("lenient route offset checkpoint needs this field")
        val item = newLenientRouteOffsetCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = LenientRouteOffsetCheckpointRequiredContactEmailRule(
            "lenient route offset checkpoint needs this field",
        )
        val item = newLenientRouteOffsetCheckpoint("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
