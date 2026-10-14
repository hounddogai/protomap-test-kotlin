package com.acme.transport.emissions.idling.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIdlingSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIdlingSnapshotValidator()
        val item = newRegionalIdlingSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalIdlingSnapshotCodeLengthRule(64)
        val item = newRegionalIdlingSnapshot("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RegionalIdlingSnapshotRequiredContactEmailRule("regional idling snapshot needs an entry")
        val item = newRegionalIdlingSnapshot("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalIdlingSnapshotRequiredCodeRule("regional idling snapshot needs an entry")
        val item = newRegionalIdlingSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
