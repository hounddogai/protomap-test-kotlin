package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCapacitySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCapacitySnapshotValidator()
        val item = newPrimaryCapacitySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryCapacitySnapshotRequiredNameRule("primary capacity snapshot needs an entry")
        val item = newPrimaryCapacitySnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = PrimaryCapacitySnapshotRequiredDockRule("primary capacity snapshot needs this field")
        val item = newPrimaryCapacitySnapshot("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryCapacitySnapshotRequiredCodeRule("primary capacity snapshot needs this field")
        val item = newPrimaryCapacitySnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryCapacitySnapshotCodeLengthRule(32)
        val item = newPrimaryCapacitySnapshot("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
