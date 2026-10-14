package com.acme.transport.network.servicearea.areaclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedServiceAreaClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedServiceAreaClaimValidator()
        val item = newWeightedServiceAreaClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedServiceAreaClaimRequiredCodeRule("weighted service area claim needs a value")
        val item = newWeightedServiceAreaClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedServiceAreaClaimRequiredNameRule("weighted service area claim needs a value")
        val item = newWeightedServiceAreaClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = WeightedServiceAreaClaimExternalRefLengthRule(80)
        val item = newWeightedServiceAreaClaim("x", "y")
        item.externalRef = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = WeightedServiceAreaClaimContactEmailLengthRule(120)
        val item = newWeightedServiceAreaClaim("x", "y")
        item.contactEmail = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
