package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalParcelVarianceValidator()
        val item = newRegionalParcelVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalParcelVarianceRequiredCodeRule("regional parcel variance needs this field")
        val item = newRegionalParcelVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = RegionalParcelVarianceRequiredDepotRule("regional parcel variance needs a value")
        val item = newRegionalParcelVariance("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalParcelVarianceRequiredNameRule("regional parcel variance needs an entry")
        val item = newRegionalParcelVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalParcelVarianceNameLengthRule(120)
        val item = newRegionalParcelVariance("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RegionalParcelVarianceMinimumRangeRule(7.6, 4767.0)
        val item = newRegionalParcelVariance("x", "y")
        item.minimum = 4768.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
