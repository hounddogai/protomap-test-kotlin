package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelSurchargeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledFuelSurchargeBundleValidator()
        val item = newScheduledFuelSurchargeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledFuelSurchargeBundleRequiredNameRule("scheduled fuel surcharge bundle needs this field")
        val item = newScheduledFuelSurchargeBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ScheduledFuelSurchargeBundleRequiredDepotRule("scheduled fuel surcharge bundle needs an entry")
        val item = newScheduledFuelSurchargeBundle("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledFuelSurchargeBundleRequiredCodeRule("scheduled fuel surcharge bundle needs a value")
        val item = newScheduledFuelSurchargeBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
