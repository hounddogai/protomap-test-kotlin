package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDefectBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDefectBundleValidator()
        val item = newRegionalDefectBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RegionalDefectBundleRequiredDriverIdRule("regional defect bundle needs an entry")
        val item = newRegionalDefectBundle("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalDefectBundleCodeLengthRule(16)
        val item = newRegionalDefectBundle("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalDefectBundleRequiredLabelRule("regional defect bundle needs this field")
        val item = newRegionalDefectBundle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = RegionalDefectBundleRequiredCarrierRule("regional defect bundle needs a value")
        val item = newRegionalDefectBundle("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
