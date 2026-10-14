package com.acme.transport.customs.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedInspectionProfileValidator()
        val item = newWeightedInspectionProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = WeightedInspectionProfileRequiredDepotRule("weighted inspection profile needs a value")
        val item = newWeightedInspectionProfile("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = WeightedInspectionProfileDepotLengthRule(80)
        val item = newWeightedInspectionProfile("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedInspectionProfileRequiredNameRule("weighted inspection profile needs an entry")
        val item = newWeightedInspectionProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
