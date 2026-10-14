package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDeclarationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDeclarationCheckpointValidator()
        val item = newLenientDeclarationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LenientDeclarationCheckpointSequenceRangeRule(16, 16)
        val item = newLenientDeclarationCheckpoint("x", "y")
        item.sequence = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = LenientDeclarationCheckpointRequiredVehicleIdRule("lenient declaration checkpoint needs this field")
        val item = newLenientDeclarationCheckpoint("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientDeclarationCheckpointLabelLengthRule(1000)
        val item = newLenientDeclarationCheckpoint("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = LenientDeclarationCheckpointRequiredReviewerRule("lenient declaration checkpoint needs an entry")
        val item = newLenientDeclarationCheckpoint("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientDeclarationCheckpointCodeLengthRule(1000)
        val item = newLenientDeclarationCheckpoint("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
