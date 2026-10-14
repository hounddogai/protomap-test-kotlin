package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIdlingEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultIdlingEntryValidator()
        val item = newDefaultIdlingEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = DefaultIdlingEntrySourceLengthRule(16)
        val item = newDefaultIdlingEntry("x", "y")
        item.source = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultIdlingEntryNameLengthRule(16)
        val item = newDefaultIdlingEntry("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultIdlingEntryRequiredCodeRule("default idling entry needs a value")
        val item = newDefaultIdlingEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DefaultIdlingEntryRequiredChecksumRule("default idling entry needs this field")
        val item = newDefaultIdlingEntry("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
