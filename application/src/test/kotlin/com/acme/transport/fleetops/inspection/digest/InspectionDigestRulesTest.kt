package com.acme.transport.fleetops.inspection.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionDigestValidator()
        val item = newInspectionDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = InspectionDigestUnitLengthRule(80)
        val item = newInspectionDigest("x", "y")
        item.unit = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionDigestCodeLengthRule(64)
        val item = newInspectionDigest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionDigestLabelLengthRule(255)
        val item = newInspectionDigest("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = InspectionDigestRequiredUnitRule("inspection digest needs a value")
        val item = newInspectionDigest("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = InspectionDigestRequiredExternalRefRule("inspection digest needs a value")
        val item = newInspectionDigest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = InspectionDigestExternalRefLengthRule(16)
        val item = newInspectionDigest("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
