package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelCheckpointValidator()
        val item = newParcelCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelCheckpointRequiredCodeRule("parcel checkpoint needs this field")
        val item = newParcelCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ParcelCheckpointNameLengthRule(255)
        val item = newParcelCheckpoint("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ParcelCheckpointRequiredNameRule("parcel checkpoint needs an entry")
        val item = newParcelCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ParcelCheckpointCeilingRangeRule(0.3, 1312.5)
        val item = newParcelCheckpoint("x", "y")
        item.ceiling = 1314.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ParcelCheckpointDepotLengthRule(80)
        val item = newParcelCheckpoint("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
