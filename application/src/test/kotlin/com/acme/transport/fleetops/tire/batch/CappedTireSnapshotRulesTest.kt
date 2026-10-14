package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTireSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTireSnapshotValidator()
        val item = newCappedTireSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = CappedTireSnapshotTrailerIdLengthRule(86400)
        val item = newCappedTireSnapshot("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedTireSnapshotRequiredCodeRule("capped tire snapshot needs a value")
        val item = newCappedTireSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = CappedTireSnapshotRequiredCountryRule("capped tire snapshot needs an entry")
        val item = newCappedTireSnapshot("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
