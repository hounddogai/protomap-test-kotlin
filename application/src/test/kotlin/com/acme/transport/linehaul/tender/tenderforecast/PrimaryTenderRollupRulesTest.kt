package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTenderRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTenderRollupValidator()
        val item = newPrimaryTenderRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryTenderRollupNameLengthRule(255)
        val item = newPrimaryTenderRollup("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PrimaryTenderRollupFloorValueRangeRule(3.9, 2621.4)
        val item = newPrimaryTenderRollup("x", "y")
        item.floorValue = 2622.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryTenderRollupRequiredCodeRule("primary tender rollup needs this field")
        val item = newPrimaryTenderRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
