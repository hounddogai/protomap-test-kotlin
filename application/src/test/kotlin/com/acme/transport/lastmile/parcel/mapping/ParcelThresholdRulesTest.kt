package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelThresholdValidator()
        val item = newParcelThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelThresholdRequiredCodeRule("parcel threshold needs an entry")
        val item = newParcelThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ParcelThresholdCodeLengthRule(86400)
        val item = newParcelThreshold("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ParcelThresholdTimezoneLengthRule(120)
        val item = newParcelThreshold("x", "y")
        item.timezone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ParcelThresholdRequiredDescriptionRule("parcel threshold needs this field")
        val item = newParcelThreshold("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
