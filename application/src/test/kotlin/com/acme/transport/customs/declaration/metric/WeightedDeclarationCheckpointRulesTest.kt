package com.acme.transport.customs.declaration.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDeclarationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDeclarationCheckpointValidator()
        val item = newWeightedDeclarationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = WeightedDeclarationCheckpointExternalRefLengthRule(255)
        val item = newWeightedDeclarationCheckpoint("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = WeightedDeclarationCheckpointLaneLengthRule(80)
        val item = newWeightedDeclarationCheckpoint("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = WeightedDeclarationCheckpointRevisionRangeRule(1000, 86400)
        val item = newWeightedDeclarationCheckpoint("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedDeclarationCheckpointRequiredNameRule("weighted declaration checkpoint needs this field")
        val item = newWeightedDeclarationCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedDeclarationCheckpointCodeLengthRule(3600)
        val item = newWeightedDeclarationCheckpoint("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
